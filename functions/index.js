/**
 * Bhakti payments backend: Razorpay subscriptions (UPI AutoPay etc.).
 *
 * The app never sees the Razorpay key secret. It asks these functions to
 * create a subscription, opens Razorpay Checkout with the returned id, and
 * the result is recorded in Firestore `entitlements/{uid}`, which the app
 * reads (and only its own user can read - see firestore.rules).
 *
 *   createSubscription   callable  - start checkout for a plan
 *   verifySubscription   callable  - confirm the Checkout success callback
 *   cancelSubscription   callable  - stop renewal at the end of the period
 *   razorpayWebhook      HTTP      - Razorpay's server-to-server events
 *                                    (renewals, failures, cancellations)
 */
const { onCall, onRequest, HttpsError } = require('firebase-functions/v2/https');
const { defineSecret, defineString } = require('firebase-functions/params');
const logger = require('firebase-functions/logger');
const admin = require('firebase-admin');
const {
  verifyPaymentSignature,
  verifyWebhookSignature,
  entitlementFromSubscription,
  razorpayRequest
} = require('./razorpay');

admin.initializeApp();
const db = admin.firestore();

// Key id is public (it ships in the app too); secrets are set with
// `firebase functions:secrets:set ...` and never stored in the repo.
const RAZORPAY_KEY_ID = defineString('RAZORPAY_KEY_ID');
const RAZORPAY_KEY_SECRET = defineSecret('RAZORPAY_KEY_SECRET');
const RAZORPAY_WEBHOOK_SECRET = defineSecret('RAZORPAY_WEBHOOK_SECRET');

const REGION = 'asia-south1'; // Mumbai - closest to Indian users and Razorpay
const callableOpts = { region: REGION, secrets: [RAZORPAY_KEY_SECRET] };

// How many billing cycles a subscription runs before Razorpay ends it, when
// the plan document doesn't set `totalCount`.
const DEFAULT_TOTAL_COUNT = { MONTHLY: 120, ANNUAL: 10 };

function requireUid(request) {
  const uid = request.auth?.uid;
  if (!uid) throw new HttpsError('unauthenticated', 'Sign in to subscribe.');
  return uid;
}

function rzp(method, path, body) {
  return razorpayRequest(RAZORPAY_KEY_ID.value(), RAZORPAY_KEY_SECRET.value(), method, path, body);
}

/** Writes `entitlements/{uid}` from a Razorpay subscription entity. */
async function saveEntitlement(uid, planKey, sub, eventCreatedAt = null) {
  const { status, hasAccess, currentPeriodEndMs } = entitlementFromSubscription(sub);
  const data = {
    subscriptionId: sub.id,
    planKey,
    status,
    hasAccess,
    currentPeriodEnd: currentPeriodEndMs ? admin.firestore.Timestamp.fromMillis(currentPeriodEndMs) : null,
    updatedAt: admin.firestore.FieldValue.serverTimestamp()
  };
  if (eventCreatedAt) data.lastEventAt = eventCreatedAt;
  await db.collection('entitlements').doc(uid).set(data, { merge: true });
  await db.collection('razorpaySubscriptions').doc(sub.id).set(
    { status, updatedAt: admin.firestore.FieldValue.serverTimestamp() },
    { merge: true }
  );
  return { status, hasAccess, currentPeriodEndMs };
}

exports.createSubscription = onCall(callableOpts, async (request) => {
  const uid = requireUid(request);
  const planKey = request.data?.planKey;
  if (typeof planKey !== 'string' || !planKey) {
    throw new HttpsError('invalid-argument', 'planKey is required.');
  }

  const planSnap = await db.collection('subscriptionPlans').doc(planKey).get();
  const plan = planSnap.data();
  if (!plan?.razorpayPlanId) {
    throw new HttpsError('failed-precondition', `Plan "${planKey}" has no razorpayPlanId set in Firestore.`);
  }

  const current = (await db.collection('entitlements').doc(uid).get()).data();
  if (current?.hasAccess && ['authenticated', 'active', 'pending'].includes(current.status)) {
    throw new HttpsError('already-exists', 'You already have an active subscription.');
  }

  const body = {
    plan_id: plan.razorpayPlanId,
    total_count: plan.totalCount || DEFAULT_TOTAL_COUNT[plan.cycle] || 12,
    quantity: 1,
    customer_notify: 1,
    notes: { uid, planKey }
  };
  // Optional free/₹1 trial: the mandate is authorised now, first charge later.
  if (plan.trialDays > 0) {
    body.start_at = Math.floor(Date.now() / 1000) + plan.trialDays * 24 * 60 * 60;
  }

  let sub;
  try {
    sub = await rzp('POST', '/subscriptions', body);
  } catch (err) {
    logger.error('createSubscription failed', { uid, planKey, error: err.message });
    throw new HttpsError('internal', 'Could not start the subscription. Please try again.');
  }

  await db.collection('razorpaySubscriptions').doc(sub.id).set({
    uid,
    planKey,
    status: sub.status,
    createdAt: admin.firestore.FieldValue.serverTimestamp()
  });

  return { subscriptionId: sub.id, keyId: RAZORPAY_KEY_ID.value() };
});

exports.verifySubscription = onCall(callableOpts, async (request) => {
  const uid = requireUid(request);
  const { paymentId, subscriptionId, signature } = request.data || {};

  if (!verifyPaymentSignature({ paymentId, subscriptionId, signature }, RAZORPAY_KEY_SECRET.value())) {
    throw new HttpsError('permission-denied', 'Payment could not be verified.');
  }

  const record = (await db.collection('razorpaySubscriptions').doc(subscriptionId).get()).data();
  if (!record || record.uid !== uid) {
    throw new HttpsError('permission-denied', 'This subscription belongs to another account.');
  }

  // Ask Razorpay for the authoritative status instead of trusting the client.
  const sub = await rzp('GET', `/subscriptions/${subscriptionId}`);
  const result = await saveEntitlement(uid, record.planKey, sub);
  return { status: result.status, hasAccess: result.hasAccess, currentPeriodEnd: result.currentPeriodEndMs };
});

exports.cancelSubscription = onCall(callableOpts, async (request) => {
  const uid = requireUid(request);
  const ent = (await db.collection('entitlements').doc(uid).get()).data();
  if (!ent?.subscriptionId) {
    throw new HttpsError('not-found', 'No subscription to cancel.');
  }
  // Cancel at cycle end: no further charges, access continues until the
  // period that was already paid for is over.
  const sub = await rzp('POST', `/subscriptions/${ent.subscriptionId}/cancel`, { cancel_at_cycle_end: 1 });
  const result = await saveEntitlement(uid, ent.planKey, sub);
  return { status: result.status, hasAccess: result.hasAccess, currentPeriodEnd: result.currentPeriodEndMs };
});

exports.razorpayWebhook = onRequest(
  { region: REGION, secrets: [RAZORPAY_WEBHOOK_SECRET] },
  async (req, res) => {
    if (req.method !== 'POST') {
      res.status(405).send('Method not allowed');
      return;
    }
    const signature = req.get('x-razorpay-signature');
    if (!verifyWebhookSignature(req.rawBody, signature, RAZORPAY_WEBHOOK_SECRET.value())) {
      logger.warn('Webhook rejected: bad signature');
      res.status(400).send('Invalid signature');
      return;
    }

    const event = req.body || {};
    const sub = event.payload?.subscription?.entity;
    if (!event.event?.startsWith('subscription.') || !sub) {
      res.status(200).send('Ignored');
      return;
    }

    // Razorpay retries deliveries; record each event id once.
    const eventId = req.get('x-razorpay-event-id');
    if (eventId) {
      try {
        await db.collection('razorpayWebhookEvents').doc(eventId).create({
          event: event.event,
          subscriptionId: sub.id,
          receivedAt: admin.firestore.FieldValue.serverTimestamp()
        });
      } catch (err) {
        if (err.code === 6) { // ALREADY_EXISTS
          res.status(200).send('Duplicate');
          return;
        }
        throw err;
      }
    }

    const record = (await db.collection('razorpaySubscriptions').doc(sub.id).get()).data();
    const uid = record?.uid || sub.notes?.uid;
    const planKey = record?.planKey || sub.notes?.planKey || null;
    if (!uid) {
      logger.warn('Webhook for unknown subscription', { subscriptionId: sub.id, event: event.event });
      res.status(200).send('Unknown subscription');
      return;
    }

    const current = (await db.collection('entitlements').doc(uid).get()).data();
    // Events can arrive out of order - never let an older event overwrite a newer one.
    if (current?.subscriptionId === sub.id && current.lastEventAt && event.created_at < current.lastEventAt) {
      res.status(200).send('Stale');
      return;
    }
    // Events for a different subscription than the one on record (e.g. an old
    // one finally expiring after the user re-subscribed) may grant access but
    // never take it away.
    if (
      current?.subscriptionId &&
      current.subscriptionId !== sub.id &&
      current.hasAccess &&
      !entitlementFromSubscription(sub).hasAccess
    ) {
      res.status(200).send('Superseded');
      return;
    }

    await saveEntitlement(uid, planKey, sub, event.created_at);
    logger.info('Webhook applied', { uid, subscriptionId: sub.id, event: event.event, status: sub.status });
    res.status(200).send('OK');
  }
);
