const { test } = require('node:test');
const assert = require('node:assert/strict');
const crypto = require('crypto');
const {
  verifyPaymentSignature,
  verifyWebhookSignature,
  entitlementFromSubscription
} = require('../razorpay');

const hmac = (secret, data) => crypto.createHmac('sha256', secret).update(data).digest('hex');

test('payment signature: valid passes, tampered or missing fails', () => {
  const secret = 'test_secret';
  const paymentId = 'pay_123';
  const subscriptionId = 'sub_456';
  const signature = hmac(secret, `${paymentId}|${subscriptionId}`);

  assert.equal(verifyPaymentSignature({ paymentId, subscriptionId, signature }, secret), true);
  assert.equal(verifyPaymentSignature({ paymentId, subscriptionId: 'sub_other', signature }, secret), false);
  assert.equal(verifyPaymentSignature({ paymentId, subscriptionId, signature }, 'wrong_secret'), false);
  assert.equal(verifyPaymentSignature({ paymentId, subscriptionId, signature: 'short' }, secret), false);
  assert.equal(verifyPaymentSignature({ paymentId, subscriptionId }, secret), false);
});

test('webhook signature is checked against the raw body', () => {
  const secret = 'whsec';
  const body = Buffer.from('{"event":"subscription.charged"}');
  const signature = hmac(secret, body);

  assert.equal(verifyWebhookSignature(body, signature, secret), true);
  assert.equal(verifyWebhookSignature(Buffer.from('{"event":"subscription.cancelled"}'), signature, secret), false);
  assert.equal(verifyWebhookSignature(body, undefined, secret), false);
});

test('entitlement: active-like statuses grant access', () => {
  const now = Date.UTC(2026, 9, 12);
  for (const status of ['authenticated', 'active', 'pending']) {
    assert.equal(entitlementFromSubscription({ status, current_end: null }, now).hasAccess, true, status);
  }
  assert.equal(entitlementFromSubscription({ status: 'created', current_end: null }, now).hasAccess, false);
  assert.equal(entitlementFromSubscription({ status: 'expired', current_end: null }, now).hasAccess, false);
});

test('entitlement: cancelled keeps access only until the paid period ends', () => {
  const now = Date.UTC(2026, 9, 12);
  const future = Math.floor(now / 1000) + 86400;
  const past = Math.floor(now / 1000) - 86400;

  const stillPaid = entitlementFromSubscription({ status: 'cancelled', current_end: future }, now);
  assert.equal(stillPaid.hasAccess, true);
  assert.equal(stillPaid.currentPeriodEndMs, future * 1000);

  assert.equal(entitlementFromSubscription({ status: 'cancelled', current_end: past }, now).hasAccess, false);
  assert.equal(entitlementFromSubscription({ status: 'halted', current_end: past }, now).hasAccess, false);
});
