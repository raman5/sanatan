/**
 * Razorpay helpers with no Firebase dependencies, so they can be unit
 * tested directly (see test/razorpay.test.js).
 */
const crypto = require('crypto');

const API_BASE = 'https://api.razorpay.com/v1';

/** Constant-time comparison of two hex signatures. */
function safeEqualHex(expected, actual) {
  if (typeof actual !== 'string' || actual.length !== expected.length) return false;
  return crypto.timingSafeEqual(Buffer.from(expected, 'utf8'), Buffer.from(actual, 'utf8'));
}

function hmacHex(secret, data) {
  return crypto.createHmac('sha256', secret).update(data).digest('hex');
}

/**
 * Checkout success callback for subscriptions: Razorpay signs
 * "<payment_id>|<subscription_id>" with the key secret.
 */
function verifyPaymentSignature({ paymentId, subscriptionId, signature }, keySecret) {
  if (!paymentId || !subscriptionId || !signature) return false;
  return safeEqualHex(hmacHex(keySecret, `${paymentId}|${subscriptionId}`), signature);
}

/** Webhooks: Razorpay signs the raw request body with the webhook secret. */
function verifyWebhookSignature(rawBody, signature, webhookSecret) {
  if (!rawBody || !signature) return false;
  return safeEqualHex(hmacHex(webhookSecret, rawBody), signature);
}

// Statuses where the user is in good standing ("pending" = a renewal charge
// is being retried; Razorpay keeps the subscription alive meanwhile).
const ACCESS_STATUSES = new Set(['authenticated', 'active', 'pending']);
// Statuses that end the subscription, but the user keeps access until the
// end of the period they already paid for.
const PAID_THROUGH_STATUSES = new Set(['cancelled', 'halted', 'paused', 'completed']);

/**
 * What a Razorpay subscription entity means for the user's access.
 * `currentPeriodEndMs` is null until the first charge (e.g. during a trial).
 */
function entitlementFromSubscription(sub, nowMs = Date.now()) {
  const currentPeriodEndMs = sub.current_end ? sub.current_end * 1000 : null;
  const hasAccess =
    ACCESS_STATUSES.has(sub.status) ||
    (PAID_THROUGH_STATUSES.has(sub.status) && currentPeriodEndMs !== null && currentPeriodEndMs > nowMs);
  return { status: sub.status, hasAccess, currentPeriodEndMs };
}

/** Minimal Razorpay REST client (Basic auth with key id + secret). */
async function razorpayRequest(keyId, keySecret, method, path, body) {
  const res = await fetch(`${API_BASE}${path}`, {
    method,
    headers: {
      Authorization: 'Basic ' + Buffer.from(`${keyId}:${keySecret}`).toString('base64'),
      'Content-Type': 'application/json'
    },
    body: body ? JSON.stringify(body) : undefined
  });
  const json = await res.json().catch(() => ({}));
  if (!res.ok) {
    const description = json?.error?.description || `HTTP ${res.status}`;
    const err = new Error(`Razorpay ${method} ${path} failed: ${description}`);
    err.status = res.status;
    throw err;
  }
  return json;
}

module.exports = {
  verifyPaymentSignature,
  verifyWebhookSignature,
  entitlementFromSubscription,
  razorpayRequest
};
