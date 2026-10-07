package com.bhakti.app.data.repository

import com.bhakti.app.data.model.Bhajan
import com.bhakti.app.data.model.Deity
import com.bhakti.app.data.model.Festival
import com.bhakti.app.data.model.Mantra
import com.bhakti.app.data.model.StatusMediaType
import com.bhakti.app.data.model.Wallpaper
import com.bhakti.app.data.model.WhatsAppStatus

/**
 * Placeholder catalogue standing in for the admin CMS. Every deity gets a
 * small, consistent set of items so Explore/search/favourites have real
 * breadth to browse. Swap this object for a CMS API client later.
 */
internal object SampleContent {

    private val themes = listOf("Devotional", "Festival Special", "Minimalist", "Golden Hour", "Temple Art")
    private val styles = listOf("Realistic", "Traditional Art", "Modern Illustration", "Gold Foil")
    private val wallpaperCategories = listOf("Daily Darshan", "Festival", "HD Portrait", "Home Screen")
    private val bhajanCategories = listOf("Aarti", "Bhajan", "Chalisa", "Stotra")
    private val statusCategories = listOf("Good Morning", "Good Night", "Deity Special", "Quotes", "Festival")

    private data class MantraSeed(val purpose: String, val devanagari: String, val transliteration: String, val meaning: String)

    // Widely known, commonly chanted lines - not scripture excerpts requiring citation.
    private val mantraSeeds: Map<Deity, MantraSeed> = mapOf(
        Deity.SHIVA to MantraSeed("Peace & inner strength", "ॐ नमः शिवाय", "Om Namah Shivaya", "Salutations to Shiva, the auspicious one within all things."),
        Deity.KRISHNA to MantraSeed("Devotion & joy", "हरे कृष्ण हरे कृष्ण कृष्ण कृष्ण हरे हरे", "Hare Krishna Hare Krishna Krishna Krishna Hare Hare", "A chant of loving devotion to Krishna."),
        Deity.RAM to MantraSeed("Courage & righteousness", "श्री राम जय राम जय जय राम", "Shri Ram Jai Ram Jai Jai Ram", "Victory to Lord Ram, invoked for strength and dharma."),
        Deity.HANUMAN to MantraSeed("Protection & courage", "ॐ हनुमते नमः", "Om Hanumate Namah", "Salutations to Hanuman, remover of obstacles and fear."),
        Deity.GANESH to MantraSeed("New beginnings", "ॐ गं गणपतये नमः", "Om Gan Ganapataye Namah", "Salutations to Ganesha, invoked before any new beginning."),
        Deity.DURGA to MantraSeed("Strength & protection", "ॐ दुं दुर्गायै नमः", "Om Dum Durgayei Namah", "Salutations to Durga, the protective mother goddess."),
        Deity.LAKSHMI to MantraSeed("Prosperity & abundance", "ॐ श्रीं महालक्ष्म्यै नमः", "Om Shreem Mahalakshmiyei Namah", "Salutations to Lakshmi, goddess of prosperity."),
        Deity.SARASWATI to MantraSeed("Knowledge & wisdom", "ॐ ऐं सरस्वत्यै नमः", "Om Aim Saraswatyai Namah", "Salutations to Saraswati, goddess of knowledge and the arts."),
        Deity.VISHNU to MantraSeed("Balance & protection", "ॐ नमो नारायणाय", "Om Namo Narayanaya", "Salutations to Vishnu, the preserver."),
        Deity.RADHA to MantraSeed("Devotion & love", "ॐ ह्रीं श्रीं राधिकायै नमः", "Om Hrim Shrim Radhikaye Namah", "Salutations to Shri Radhika, invoked with the bija syllables Hrim and Shrim for divine love and grace."),
        Deity.SAI_BABA to MantraSeed("Faith & patience", "ॐ साईं राम", "Om Sai Ram", "A chant of faith invoking Sai Baba's blessings."),
        Deity.JAGANNATH to MantraSeed("Devotion & surrender", "जय जगन्नाथ", "Jai Jagannath", "An invocation of Jagannath, Lord of the Universe."),
        Deity.SHANI_DEV to MantraSeed("Justice & resilience", "ॐ शं शनैश्चराय नमः", "Om Sham Shanaishcharaya Namah", "Salutations to Shani Dev, who rewards discipline and patience."),
        Deity.KALI to MantraSeed("Strength & transformation", "ॐ क्रीं कालिकायै नमः", "Om Kreem Kalikayei Namah", "Salutations to Kali, the fierce protective mother."),
        Deity.BALAJI to MantraSeed("Devotion & fulfilment", "ॐ नमो वेंकटेशाय", "Om Namo Venkatesaya", "Salutations to Balaji (Venkateswara), fulfiller of devotees' wishes.")
    )

    // Short bold exclamations for WhatsApp status greetings - distinct from the chant in mantraSeeds.
    private val greetings: Map<Deity, String> = mapOf(
        Deity.SHIVA to "ॐ नमः शिवाय",
        Deity.KRISHNA to "जय श्री कृष्णा",
        Deity.RAM to "जय श्री राम",
        Deity.HANUMAN to "जय हनुमान",
        Deity.GANESH to "जय गणेश",
        Deity.DURGA to "जय माँ दुर्गा",
        Deity.LAKSHMI to "जय माँ लक्ष्मी",
        Deity.SARASWATI to "जय माँ सरस्वती",
        Deity.VISHNU to "जय श्री हरि",
        Deity.RADHA to "राधे राधे",
        Deity.SAI_BABA to "ॐ साईं राम",
        Deity.JAGANNATH to "जय जगन्नाथ",
        Deity.SHANI_DEV to "जय शनि देव",
        Deity.KALI to "जय माँ काली",
        Deity.BALAJI to "जय बालाजी"
    )

    fun buildWallpapers(): List<Wallpaper> = Deity.entries.flatMapIndexed { deityIdx, deity ->
        (0 until 2).map { i ->
            val idx = deityIdx * 2 + i
            Wallpaper(
                id = "wp-${deity.name.lowercase()}-$i",
                title = "${deity.displayName} ${themes[idx % themes.size]}",
                deity = deity,
                category = wallpaperCategories[idx % wallpaperCategories.size],
                theme = themes[idx % themes.size],
                festival = if (idx % 5 == 0) "Featured Festival" else null,
                style = styles[idx % styles.size],
                tags = listOf(deity.displayName, themes[idx % themes.size], styles[idx % styles.size]),
                featured = i == 0,
                imageVariant = i
            )
        }
    }

    fun buildBhajans(): List<Bhajan> = Deity.entries.mapIndexed { idx, deity ->
        Bhajan(
            id = "bh-${deity.name.lowercase()}",
            title = "${deity.displayName} ${bhajanCategories[idx % bhajanCategories.size]}",
            deity = deity,
            category = bhajanCategories[idx % bhajanCategories.size],
            singer = "Various Artists",
            // Short clips (spoken recitation, not a full sung track)
            durationSec = 3 + idx % 4,
            playCount = 1000 + idx * 137,
            featured = idx % 4 == 0
        )
    }

    fun buildMantras(): List<Mantra> = Deity.entries.flatMap { deity ->
        val seed = mantraSeeds.getValue(deity)
        listOf(
            Mantra(
                id = "mn-${deity.name.lowercase()}-1",
                title = "${deity.displayName} Mantra",
                deity = deity,
                purpose = seed.purpose,
                category = "Daily Chant",
                devanagari = seed.devanagari,
                transliteration = seed.transliteration,
                meaning = seed.meaning
            )
        )
    }

    fun buildStatuses(): List<WhatsAppStatus> = Deity.entries.flatMapIndexed { deityIdx, deity ->
        (0 until 2).map { i ->
            val idx = deityIdx * 2 + i
            val mediaType = StatusMediaType.entries[idx % StatusMediaType.entries.size]
            WhatsAppStatus(
                id = "st-${deity.name.lowercase()}-$i",
                title = "${deity.displayName} ${statusCategories[idx % statusCategories.size]}",
                deity = deity,
                mediaType = mediaType,
                category = statusCategories[idx % statusCategories.size],
                caption = "Jai ${deity.displayName}! Sharing blessings for your day.",
                greeting = greetings.getValue(deity),
                // Every second status also carries the deity's short chant as a spiritual message.
                shloka = if (i == 1) mantraSeeds.getValue(deity).devanagari else null,
                shareCount = 200 + idx * 41,
                imageVariant = i
            )
        }
    }

    fun buildFestivals(): List<Festival> = listOf(
        Festival("fest-navratri", "Navratri", "2026-10-11", Deity.DURGA),
        Festival("fest-diwali", "Diwali", "2026-11-08", Deity.LAKSHMI),
        Festival("fest-ganesh-chaturthi", "Ganesh Chaturthi", "2026-09-14", Deity.GANESH),
        Festival("fest-janmashtami", "Krishna Janmashtami", "2027-08-24", Deity.KRISHNA),
        Festival("fest-maha-shivratri", "Maha Shivratri", "2027-02-15", Deity.SHIVA),
        Festival("fest-hanuman-jayanti", "Hanuman Jayanti", "2027-04-01", Deity.HANUMAN)
    )
}
