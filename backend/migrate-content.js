#!/usr/bin/env node
/**
 * One-time migration: uploads Bhakti's bundled deity portraits and
 * mantra/bell audio to Firebase Storage, and writes the matching Firestore
 * documents (wallpapers, mantras, statuses, festivals,
 * deityPortraits, appAssets, subscriptionPlans) with those URLs filled in.
 *
 * Mirrors app/src/main/java/com/bhakti/app/data/repository/SampleContent.kt
 * field-for-field, including ids, so content keeps working exactly as it
 * does today once the Android app switches over to FirebaseContentRepository
 * - it isn't a redesign of the catalogue, just moving today's catalogue off
 * local resources and onto a backend you can edit without a release.
 *
 * Usage:
 *   1. In Firebase Console: create a project, enable Firestore (Native
 *      mode) and Storage.
 *   2. Project Settings > Service Accounts > "Generate new private key" ->
 *      save as backend/serviceAccountKey.json (git-ignored already).
 *   3. cd backend && npm install
 *   4. node migrate-content.js <your-project-id>.appspot.com
 *      (the Storage bucket name - Firebase Console > Storage shows it)
 *
 * Safe to re-run: every write is an upsert (Firestore .set(), Storage
 * objects overwritten at the same path), so running it again after editing
 * this script's data just syncs forward.
 */
const admin = require('firebase-admin');
const crypto = require('crypto');
const fs = require('fs');
const path = require('path');

const bucketName = process.argv[2];
if (!bucketName) {
  console.error('Usage: node migrate-content.js <storage-bucket-name>');
  console.error('Find it in Firebase Console > Storage (looks like your-project.appspot.com)');
  process.exit(1);
}

const serviceAccountPath = path.join(__dirname, 'serviceAccountKey.json');
if (!fs.existsSync(serviceAccountPath)) {
  console.error(`Missing ${serviceAccountPath}`);
  console.error('Firebase Console > Project Settings > Service Accounts > Generate new private key');
  process.exit(1);
}

admin.initializeApp({
  credential: admin.credential.cert(require(serviceAccountPath)),
  storageBucket: bucketName
});
const bucket = admin.storage().bucket();
const db = admin.firestore();

const RES_DIR = path.join(__dirname, '..', 'app', 'src', 'main', 'res');
const DRAWABLE_DIR = path.join(RES_DIR, 'drawable-nodpi');
const RAW_DIR = path.join(RES_DIR, 'raw');

// --- Deity catalogue (order + names must match Deity.kt) --------------------

const DEITIES = [
  'SHIVA', 'KRISHNA', 'RAM', 'HANUMAN', 'GANESH', 'DURGA', 'LAKSHMI',
  'SARASWATI', 'VISHNU', 'RADHA', 'SAI_BABA', 'JAGANNATH', 'SHANI_DEV',
  'KALI', 'BALAJI'
];
const DISPLAY_NAMES = {
  SHIVA: 'Shiva', KRISHNA: 'Krishna', RAM: 'Ram', HANUMAN: 'Hanuman',
  GANESH: 'Ganesh', DURGA: 'Durga', LAKSHMI: 'Lakshmi', SARASWATI: 'Saraswati',
  VISHNU: 'Vishnu', RADHA: 'Radha', SAI_BABA: 'Sai Baba', JAGANNATH: 'Jagannath',
  SHANI_DEV: 'Shani Dev', KALI: 'Kali', BALAJI: 'Balaji'
};

// --- Content seed data (mirrors SampleContent.kt exactly) -------------------

const THEMES = ['Devotional', 'Festival Special', 'Minimalist', 'Golden Hour', 'Temple Art'];
const STYLES = ['Realistic', 'Traditional Art', 'Modern Illustration', 'Gold Foil'];
const WALLPAPER_CATEGORIES = ['Daily Darshan', 'Festival', 'HD Portrait', 'Home Screen'];
const STATUS_CATEGORIES = ['Good Morning', 'Good Night', 'Deity Special', 'Quotes', 'Festival'];
const STATUS_MEDIA_TYPES = ['IMAGE', 'VIDEO', 'TEXT'];

const MANTRA_SEEDS = {
  SHIVA: { purpose: 'Peace & inner strength', devanagari: 'ॐ नमः शिवाय', transliteration: 'Om Namah Shivaya', meaning: 'Salutations to Shiva, the auspicious one within all things.' },
  KRISHNA: { purpose: 'Devotion & joy', devanagari: 'हरे कृष्ण हरे कृष्ण कृष्ण कृष्ण हरे हरे', transliteration: 'Hare Krishna Hare Krishna Krishna Krishna Hare Hare', meaning: 'A chant of loving devotion to Krishna.' },
  RAM: { purpose: 'Courage & righteousness', devanagari: 'श्री राम जय राम जय जय राम', transliteration: 'Shri Ram Jai Ram Jai Jai Ram', meaning: 'Victory to Lord Ram, invoked for strength and dharma.' },
  HANUMAN: { purpose: 'Protection & courage', devanagari: 'ॐ हनुमते नमः', transliteration: 'Om Hanumate Namah', meaning: 'Salutations to Hanuman, remover of obstacles and fear.' },
  GANESH: { purpose: 'New beginnings', devanagari: 'ॐ गं गणपतये नमः', transliteration: 'Om Gan Ganapataye Namah', meaning: 'Salutations to Ganesha, invoked before any new beginning.' },
  DURGA: { purpose: 'Strength & protection', devanagari: 'ॐ दुं दुर्गायै नमः', transliteration: 'Om Dum Durgayei Namah', meaning: 'Salutations to Durga, the protective mother goddess.' },
  LAKSHMI: { purpose: 'Prosperity & abundance', devanagari: 'ॐ श्रीं महालक्ष्म्यै नमः', transliteration: 'Om Shreem Mahalakshmiyei Namah', meaning: 'Salutations to Lakshmi, goddess of prosperity.' },
  SARASWATI: { purpose: 'Knowledge & wisdom', devanagari: 'ॐ ऐं सरस्वत्यै नमः', transliteration: 'Om Aim Saraswatyai Namah', meaning: 'Salutations to Saraswati, goddess of knowledge and the arts.' },
  VISHNU: { purpose: 'Balance & protection', devanagari: 'ॐ नमो नारायणाय', transliteration: 'Om Namo Narayanaya', meaning: 'Salutations to Vishnu, the preserver.' },
  RADHA: { purpose: 'Devotion & love', devanagari: 'ॐ ह्रीं श्रीं राधिकायै नमः', transliteration: 'Om Hrim Shrim Radhikaye Namah', meaning: 'Salutations to Shri Radhika, invoked with the bija syllables Hrim and Shrim for divine love and grace.' },
  SAI_BABA: { purpose: 'Faith & patience', devanagari: 'ॐ साईं राम', transliteration: 'Om Sai Ram', meaning: "A chant of faith invoking Sai Baba's blessings." },
  JAGANNATH: { purpose: 'Devotion & surrender', devanagari: 'जय जगन्नाथ', transliteration: 'Jai Jagannath', meaning: 'An invocation of Jagannath, Lord of the Universe.' },
  SHANI_DEV: { purpose: 'Justice & resilience', devanagari: 'ॐ शं शनैश्चराय नमः', transliteration: 'Om Sham Shanaishcharaya Namah', meaning: 'Salutations to Shani Dev, who rewards discipline and patience.' },
  KALI: { purpose: 'Strength & transformation', devanagari: 'ॐ क्रीं कालिकायै नमः', transliteration: 'Om Kreem Kalikayei Namah', meaning: 'Salutations to Kali, the fierce protective mother.' },
  BALAJI: { purpose: 'Devotion & fulfilment', devanagari: 'ॐ नमो वेंकटेशाय', transliteration: 'Om Namo Venkatesaya', meaning: "Salutations to Balaji (Venkateswara), fulfiller of devotees' wishes." }
};

const GREETINGS = {
  SHIVA: 'ॐ नमः शिवाय', KRISHNA: 'जय श्री कृष्णा', RAM: 'जय श्री राम', HANUMAN: 'जय हनुमान',
  GANESH: 'जय गणेश', DURGA: 'जय माँ दुर्गा', LAKSHMI: 'जय माँ लक्ष्मी', SARASWATI: 'जय माँ सरस्वती',
  VISHNU: 'जय श्री हरि', RADHA: 'राधे राधे', SAI_BABA: 'ॐ साईं राम', JAGANNATH: 'जय जगन्नाथ',
  SHANI_DEV: 'जय शनि देव', KALI: 'जय माँ काली', BALAJI: 'जय बालाजी'
};

const FESTIVALS = [
  { id: 'fest-navratri', name: 'Navratri', dateIso: '2026-10-11', deity: 'DURGA' },
  { id: 'fest-diwali', name: 'Diwali', dateIso: '2026-11-08', deity: 'LAKSHMI' },
  { id: 'fest-ganesh-chaturthi', name: 'Ganesh Chaturthi', dateIso: '2026-09-14', deity: 'GANESH' },
  { id: 'fest-janmashtami', name: 'Krishna Janmashtami', dateIso: '2027-08-24', deity: 'KRISHNA' },
  { id: 'fest-maha-shivratri', name: 'Maha Shivratri', dateIso: '2027-02-15', deity: 'SHIVA' },
  { id: 'fest-hanuman-jayanti', name: 'Hanuman Jayanti', dateIso: '2027-04-01', deity: 'HANUMAN' }
];

const SUBSCRIPTION_PLANS = [
  { id: 'monthly', cycle: 'MONTHLY', label: 'Monthly', priceRupees: 99, perMonthEquivalent: 99 },
  { id: 'annual', cycle: 'ANNUAL', label: 'Annual', priceRupees: 599, perMonthEquivalent: Math.floor(599 / 12), badge: 'Best value' }
];

// --- Upload helper ------------------------------------------------------

/** Uploads a local file and returns a stable getDownloadURL()-style link (token-based, works with default Storage Rules once they allow read on this path). */
async function uploadAndGetUrl(localPath, destPath, contentType) {
  const token = crypto.randomUUID();
  await bucket.upload(localPath, {
    destination: destPath,
    // Long-lived caching: each upload gets a fresh token URL, so a replaced
    // file is a new URL anyway - the app can safely cache these forever.
    metadata: { contentType, cacheControl: 'public, max-age=31536000, immutable', metadata: { firebaseStorageDownloadTokens: token } }
  });
  return `https://firebasestorage.googleapis.com/v0/b/${bucket.name}/o/${encodeURIComponent(destPath)}?alt=media&token=${token}`;
}

function lower(deityEnumName) {
  return deityEnumName.toLowerCase();
}

async function main() {
  console.log(`Uploading to gs://${bucket.name} ...`);

  // 1) Deity portraits (2 images per deity - these same URLs are reused
  //    below as wallpaper/status artwork, exactly like imageFor(deity, variant)
  //    reuses the same two local drawables today).
  const portraitUrls = {}; // deity -> { primary, secondary }
  for (const deity of DEITIES) {
    const file1 = path.join(DRAWABLE_DIR, `deity_${lower(deity)}.jpg`);
    const file2 = path.join(DRAWABLE_DIR, `deity_${lower(deity)}_2.jpg`);
    const primary = await uploadAndGetUrl(file1, `deityPortraits/${lower(deity)}.jpg`, 'image/jpeg');
    const secondary = await uploadAndGetUrl(file2, `deityPortraits/${lower(deity)}_2.jpg`, 'image/jpeg');
    portraitUrls[deity] = { primary, secondary };
    await db.collection('deityPortraits').doc(lower(deity)).set({
      portraitUrl: primary,
      secondaryPortraitUrl: secondary
    });
    console.log(`  deity portrait: ${deity}`);
  }

  // 2) Mantra audio (one file per deity).
  const mantraAudioUrls = {};
  for (const deity of DEITIES) {
    const mantraFile = path.join(RAW_DIR, `mantra_${lower(deity)}.m4a`);
    mantraAudioUrls[deity] = await uploadAndGetUrl(mantraFile, `mantraAudio/${lower(deity)}.m4a`, 'audio/mp4');
    console.log(`  audio: ${deity}`);
  }

  // 3) Bell sound effect (app-wide asset, not per-deity).
  const bellUrl = await uploadAndGetUrl(path.join(RAW_DIR, 'bell_ghanti.m4a'), 'appAssets/bell_ghanti.m4a', 'audio/mp4');
  await db.collection('appAssets').doc('bell').set({ audioUrl: bellUrl });
  console.log('  bell sound uploaded');

  // 4) Wallpapers (2 per deity).
  let batch = db.batch();
  let opCount = 0;
  const commitIfFull = async () => {
    if (opCount >= 400) { await batch.commit(); batch = db.batch(); opCount = 0; }
  };

  DEITIES.forEach((deity, deityIdx) => {
    for (let i = 0; i < 2; i++) {
      const idx = deityIdx * 2 + i;
      const id = `wp-${lower(deity)}-${i}`;
      const theme = THEMES[idx % THEMES.length];
      const style = STYLES[idx % STYLES.length];
      batch.set(db.collection('wallpapers').doc(id), {
        deity,
        title: `${DISPLAY_NAMES[deity]} ${theme}`,
        category: WALLPAPER_CATEGORIES[idx % WALLPAPER_CATEGORIES.length],
        theme,
        festival: idx % 5 === 0 ? 'Featured Festival' : null,
        style,
        tags: [DISPLAY_NAMES[deity], theme, style],
        resolution: '1080x1920',
        language: 'Hindi',
        status: 'PUBLISHED',
        featured: i === 0,
        imageVariant: i,
        imageUrl: i === 0 ? portraitUrls[deity].primary : portraitUrls[deity].secondary
      });
      opCount++;
    }
  });

  // 5) Mantras (1 per deity).
  DEITIES.forEach((deity) => {
    const seed = MANTRA_SEEDS[deity];
    batch.set(db.collection('mantras').doc(`mn-${lower(deity)}-1`), {
      deity,
      title: `${DISPLAY_NAMES[deity]} Mantra`,
      purpose: seed.purpose,
      category: 'Daily Chant',
      devanagari: seed.devanagari,
      transliteration: seed.transliteration,
      meaning: seed.meaning,
      recommendedCount: 108,
      hasAudio: true,
      durationSec: 90,
      status: 'PUBLISHED',
      audioUrl: mantraAudioUrls[deity]
    });
    opCount++;
  });

  // 6) WhatsApp statuses (2 per deity).
  DEITIES.forEach((deity, deityIdx) => {
    for (let i = 0; i < 2; i++) {
      const idx = deityIdx * 2 + i;
      const id = `st-${lower(deity)}-${i}`;
      batch.set(db.collection('statuses').doc(id), {
        deity,
        title: `${DISPLAY_NAMES[deity]} ${STATUS_CATEGORIES[idx % STATUS_CATEGORIES.length]}`,
        mediaType: STATUS_MEDIA_TYPES[idx % STATUS_MEDIA_TYPES.length],
        category: STATUS_CATEGORIES[idx % STATUS_CATEGORIES.length],
        caption: `Jai ${DISPLAY_NAMES[deity]}! Sharing blessings for your day.`,
        greeting: GREETINGS[deity],
        shloka: i === 1 ? MANTRA_SEEDS[deity].devanagari : null,
        shareCount: 200 + idx * 41,
        status: 'PUBLISHED',
        imageVariant: i,
        imageUrl: i === 0 ? portraitUrls[deity].primary : portraitUrls[deity].secondary
      });
      opCount++;
    }
  });

  // 7) Festivals.
  FESTIVALS.forEach((festival) => {
    const { id, ...rest } = festival;
    batch.set(db.collection('festivals').doc(id), rest);
    opCount++;
  });

  // 8) Subscription plans.
  SUBSCRIPTION_PLANS.forEach((plan) => {
    const { id, ...rest } = plan;
    batch.set(db.collection('subscriptionPlans').doc(id), rest);
    opCount++;
  });

  await batch.commit();
  console.log(`Done. Wrote ${DEITIES.length * 2} wallpapers, ${DEITIES.length} mantras, ${DEITIES.length * 2} statuses, ${FESTIVALS.length} festivals, ${SUBSCRIPTION_PLANS.length} subscription plans, ${DEITIES.length} deity portraits, 1 app asset.`);
}

main().catch((err) => {
  console.error('Migration failed:', err);
  process.exit(1);
});
