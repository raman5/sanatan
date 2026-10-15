# Bhakti payments (Razorpay on Cloud Functions)

Subscriptions are created and verified on the server so the Razorpay key
secret never ships in the app. Each user's result is written to Firestore
`entitlements/{uid}`, which only that user can read (`firestore.rules`).

| Function | Type | Purpose |
|---|---|---|
| `createSubscription` | callable | Create a Razorpay subscription for `{ planKey }` and return `{ subscriptionId, keyId }` for Checkout |
| `verifySubscription` | callable | Check the Checkout success signature, fetch the subscription from Razorpay, save the entitlement |
| `cancelSubscription` | callable | Cancel at the end of the current billing cycle |
| `razorpayWebhook` | HTTPS | Razorpay `subscription.*` events (renewals, failures, cancellations) |

All run in `asia-south1` (Mumbai). Webhook URL:
`https://asia-south1-bhaktt-139da.cloudfunctions.net/razorpayWebhook`

## Configuration

- `functions/.env` (git-ignored, copy from `.env.example`): `RAZORPAY_KEY_ID`.
- Secrets, set once with the Firebase CLI. Never commit them or paste them in chat:
  ```
  firebase functions:secrets:set RAZORPAY_KEY_SECRET
  firebase functions:secrets:set RAZORPAY_WEBHOOK_SECRET
  ```
  The webhook secret is a password you choose. Enter the same value in
  Razorpay → Account & Settings → Webhooks.
- Firestore `subscriptionPlans/{monthly|annual}` needs `razorpayPlanId`
  (string, from Razorpay → Subscriptions → Plans, same mode as the key).
  Optional: `trialDays` (number) delays the first charge; `totalCount`
  (number) overrides how many cycles the subscription runs.

## Firestore collections written here

- `entitlements/{uid}`: `subscriptionId`, `planKey`, `status`, `hasAccess`,
  `currentPeriodEnd`. The app treats the user as subscribed when `hasAccess`
  is true and `currentPeriodEnd` (if set) is in the future.
- `razorpaySubscriptions/{subscriptionId}`: owner uid and plan (server only).
- `razorpayWebhookEvents/{eventId}`: de-duplicates webhook retries (server only).

## Commands

```
cd functions && npm install && npm test        # unit tests
firebase deploy --only functions,firestore:rules
firebase functions:log --only razorpayWebhook  # see webhook activity
```

Switching between test and live keys: update `RAZORPAY_KEY_ID` in `.env`,
set the matching `RAZORPAY_KEY_SECRET`, use plan ids from the same mode,
and redeploy.
