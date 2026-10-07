# Bhakti backend (Firebase)

Configurable content - deity portraits, wallpaper/status artwork, bhajan and
mantra audio, the pooja bell sound, and subscription pricing - lives here,
not in the Android app. Change it from the Firebase Console any time;
no app release needed.

## What's where

- **Firestore** (structured text/numbers): `wallpapers`, `bhajans`,
  `mantras`, `statuses`, `festivals`, `deityPortraits`, `appAssets`,
  `subscriptionPlans` collections. Document shapes mirror
  `app/src/main/java/com/bhakti/app/data/repository/FirebaseContentRepository.kt`
  field-for-field.
- **Storage**: the actual image/audio files these documents link to, under
  `deityPortraits/`, `bhajanAudio/`, `mantraAudio/`, `appAssets/`.
- **The Android app**: reads both through
  `FirebaseContentRepository` once it's configured (see step 4) -
  automatically, with zero further app changes, because every screen
  already reads through the `ContentRepository` interface rather than
  hardcoded local resources.

## One-time setup

### 1. Create the Firebase project

[console.firebase.google.com](https://console.firebase.google.com) ->
Add project -> name it (e.g. "bhakti-app") -> you can skip Google
Analytics, it's not needed here.

### 2. Enable Firestore and Storage

In the new project: **Build > Firestore Database > Create database**
(Native mode, pick a region close to your users) and **Build > Storage >
Get started** (start in production mode - see the Storage Rules note
below).

### 3. Register the Android app + get its config values

Project Settings (gear icon) > your apps > Add app > Android.
- Package name: `com.bhakti.app`
- You can skip the SPM/download-config-file steps shown there - we read
  config from `firebase.properties` instead (see step 5), not
  `google-services.json`, so the Gradle build never hard-fails on a
  missing config file on a machine that hasn't set this up.
- After registering, go to **Project Settings > General**. You'll see:
  Project ID, Web API Key, App ID (for the Android app you just
  registered), and the Storage bucket name (also visible under
  **Build > Storage**, top of the page, looks like
  `your-project.appspot.com` or `your-project.firebasestorage.app`).

### 4. Create `firebase.properties`

In the repo root (next to `keystore.properties` - same git-ignored
pattern), create `firebase.properties`:

```properties
apiKey=<Web API Key from step 3>
appId=<Android App ID from step 3, looks like 1:123456789:android:abcdef>
projectId=<Project ID from step 3>
storageBucket=<Storage bucket name from step 3>
```

Rebuild the app - `AppContainer` detects these are present and switches
from `FakeContentRepository` to `FirebaseContentRepository` automatically
(see `app/src/main/java/com/bhakti/app/core/di/AppContainer.kt`).

At this point the app builds and runs, but Firestore/Storage are empty, so
every screen falls back to the bundled local drawables/audio (same as
before) because every `imageUrl`/`audioUrl` field comes back null. That's
expected until you run the migration below.

### 5. Open Storage Rules for read access

**Build > Storage > Rules**, and allow public read on the content paths
this script writes to (write stays locked down to your service account,
which bypasses rules entirely):

```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read: if true;
      allow write: if false;
    }
  }
}
```

This content (deity portraits, bhajans, mantras) is meant to be public
inside the app, so this is intentional, not a security hole - just don't
put anything sensitive in this bucket later without tightening the rule.

### 6. Get a service account key (for the migration script only)

**Project Settings > Service Accounts > Generate new private key**.
Save the downloaded file as `backend/serviceAccountKey.json` (already
git-ignored - never commit it; it's an admin credential for your whole
Firebase project).

### 7. Run the migration

```bash
cd backend
npm install
node migrate-content.js <your-storage-bucket-name>
```

This uploads every bundled deity portrait, bhajan/mantra audio clip and
the bell sound to Storage, and writes matching Firestore documents with
the same ids the app already uses (`wp-shiva-0`, `bh-krishna`, etc.) - so
this is a like-for-like move of today's catalogue onto the backend, not a
new/different catalogue. Safe to re-run any time (every write upserts).

Takes a few minutes - it's uploading ~45 images/audio files.

### 8. Verify

Reinstall the app (or just relaunch it - repository selection happens at
process start) and check a few screens: Explore > Wallpapers should now
be loading images from Storage instead of the bundled drawables (hard to
tell visually since the uploaded images are the *same* files, but you can
confirm in Android Studio's network inspector, or by editing a document
in Firestore Console and seeing the change show up in the app after a
fresh launch).

## Changing content later (the actual point of all this)

- **Swap a wallpaper's image**: upload the new file to Storage anywhere
  under `deityPortraits/` (or any path), copy its download URL (right side
  panel in Storage Console has a "Download URL" or you can construct one
  the same way `migrate-content.js` does), paste it into that wallpaper's
  `imageUrl` field in Firestore Console.
- **Change a mantra's meaning text**: edit the `meaning` field directly in
  Firestore Console - no file upload involved.
- **Change subscription pricing**: edit `priceRupees`/`perMonthEquivalent`
  on the `subscriptionPlans/monthly` or `/annual` document.
- **Give Naam Japa a recorded voice**: by default each Naam Japa tap
  speaks the deity's name with the phone's own text-to-speech. To use a
  real recording instead, upload a short clip (just the name, ~1 second)
  to Storage, and add a `naamJapaAudioUrl` field with its download URL to
  that deity's document in `deityPortraits` (e.g. `deityPortraits/radha`).
  Mantra japa works the same way: add a `japaAudioUrl` field (one recorded
  repetition of the mantra) to a document in `mantras` to replace the
  text-to-speech chant on each counter tap.
- **Add a new wallpaper/bhajan/mantra/status entirely**: create a new
  document in the relevant collection with a new id, following the same
  field shape as the existing ones (check `FirebaseContentRepository.kt`'s
  mapping functions for the exact field names each collection expects).

None of this needs a new APK/AAB or a Play Store release - the app picks
up Firestore/Storage changes the next time each screen loads that data
(Firestore's local cache means a flaky connection won't break anything
either, see `FirebaseConfig.kt`).
