# Bhakti

An Android app for daily devotional content: mantras, bhajans, devotional
wallpapers and WhatsApp status, behind a UPI AutoPay subscription.

> This repo previously scaffolded "Sanatan" (panchang/kundli/astrologer). It
> has been repurposed for Bhakti per the product PRD - see `CONTRIBUTING.md`
> for what changed and who owns what now.

## Stack

| Piece | Choice |
|---|---|
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose + Material 3 |
| Build | AGP 8.13.2, Gradle 8.13 |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 35 |

Versions are pinned in `gradle/libs.versions.toml`. They match the toolchain
Android Studio on this machine already builds with. To move to AGP 9.x and
Compose BOM 2026.08.00, update Android Studio first, then run
**Tools > AGP Upgrade Assistant**.

## Getting started

```bash
git clone <repo-url>
cd Bhakti
```

Open the folder in Android Studio and let Gradle sync. `local.properties`
is generated for you and is intentionally not in Git.

## What's real vs. mocked in this MVP

No backend, payment gateway or SMS gateway credentials exist yet, so this
build focuses on a complete, navigable app with real client-side logic
(OTP expiry/cooldown/rate-limiting, UPI ID validation, local notification
scheduling, DataStore-backed session/favourites/preferences) behind
repository interfaces:

- `data/repository/AuthRepository` - `FakeAuthRepository` mocks Google
  Sign-In and owns real OTP business rules locally. Swap in the Google
  Sign-In SDK and a real SMS/OTP provider (e.g. Firebase Auth) later.
- `data/repository/PaymentRepository` - `FakeUpiPaymentRepository`
  validates the UPI ID and simulates mandate setup. Swap in a real gateway
  (e.g. Razorpay UPI AutoPay) later.
- `data/repository/ContentRepository` - `FakeContentRepository` serves an
  in-memory catalogue (`SampleContent.kt`) covering all 15 launch deities.
  Swap in a CMS-backed API client later; the Admin CMS itself is out of
  scope for this pass.
- `notifications/NotificationScheduler` - schedules real local
  notifications (AlarmManager) with deep links back into the app; nothing
  mocked here, but alarms don't currently survive a device reboot (see the
  comment in `AndroidManifest.xml`).

## Layout

```
app/src/main/java/com/bhakti/app/
├── MainActivity.kt, BhaktiApplication.kt   entry point + composition root
├── core/navigation/                        routes + NavHost + bottom nav
├── core/session/                           DataStore-backed session state
├── core/di/                                hand-rolled AppContainer
├── core/ui/                                shared composables
├── data/model/                             data classes
├── data/repository/                        repository interfaces + fakes
├── notifications/                          local reminder scheduling
├── feature/splash, auth, paywall, payment  onboarding + subscription flow
├── feature/home                            "For You" daily dashboard
├── feature/explore                         wallpapers, bhajans, mantras, status
├── feature/search, favourites, profile     remaining nav destinations
└── ui/theme/                                colors, type, theme
```

Read `CONTRIBUTING.md` before your first commit.
