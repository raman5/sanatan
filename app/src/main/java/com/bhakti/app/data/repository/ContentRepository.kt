package com.bhakti.app.data.repository

import com.bhakti.app.data.model.Bhajan
import com.bhakti.app.data.model.ContentType
import com.bhakti.app.data.model.Deity
import com.bhakti.app.data.model.DeityPortrait
import com.bhakti.app.data.model.DevotionalContent
import com.bhakti.app.data.model.Festival
import com.bhakti.app.data.model.Mantra
import com.bhakti.app.data.model.StatusMediaType
import com.bhakti.app.data.model.SubscriptionPlan
import com.bhakti.app.data.model.Wallpaper
import com.bhakti.app.data.model.WhatsAppStatus
import kotlinx.coroutines.delay

data class SearchResults(
    val wallpapers: List<Wallpaper>,
    val mantras: List<Mantra>,
    val statuses: List<WhatsAppStatus>
) {
    val isEmpty: Boolean
        get() = wallpapers.isEmpty() && mantras.isEmpty() && statuses.isEmpty()
}

/**
 * All devotional content for Explore + Home. Backed by an in-memory
 * catalogue today; the interface is shaped so a real CMS-backed API client
 * can replace [FakeContentRepository] without touching any screen.
 */
interface ContentRepository {
    suspend fun wallpapers(deity: Deity? = null): List<Wallpaper>
    suspend fun bhajans(deity: Deity? = null): List<Bhajan>
    suspend fun mantras(deity: Deity? = null): List<Mantra>
    suspend fun statuses(deity: Deity? = null): List<WhatsAppStatus>
    suspend fun festivals(): List<Festival>

    suspend fun featuredWallpaper(): Wallpaper
    suspend fun mantraOfTheDay(): Mantra
    suspend fun bhajanOfTheDay(): Bhajan
    suspend fun statusOfTheDay(): WhatsAppStatus

    suspend fun byId(type: ContentType, id: String): DevotionalContent?
    suspend fun search(query: String): SearchResults

    /**
     * Every deity's own portrait art in one call - deliberately bulk (one
     * Firestore collection read) rather than per-deity, since every caller
     * (deity grids, Home's deity row) needs all 15 at once, not just one.
     */
    suspend fun deityPortraits(): Map<Deity, DeityPortrait>

    /** The pooja shrine's bell sound. Null falls back to the bundled raw resource. */
    suspend fun bellSoundUrl(): String?

    /** Subscription pricing/labels - configurable without a release so prices can change freely. */
    suspend fun subscriptionPlans(): List<SubscriptionPlan>
}

class FakeContentRepository : ContentRepository {

    private val allWallpapers: List<Wallpaper> = SampleContent.buildWallpapers()
    private val allBhajans: List<Bhajan> = SampleContent.buildBhajans()
    private val allMantras: List<Mantra> = SampleContent.buildMantras()
    private val allStatuses: List<WhatsAppStatus> = SampleContent.buildStatuses()
    private val allFestivals: List<Festival> = SampleContent.buildFestivals()

    override suspend fun wallpapers(deity: Deity?): List<Wallpaper> {
        delay(150)
        return allWallpapers.filter { deity == null || it.deity == deity }
    }

    override suspend fun bhajans(deity: Deity?): List<Bhajan> {
        delay(150)
        return allBhajans.filter { deity == null || it.deity == deity }
    }

    override suspend fun mantras(deity: Deity?): List<Mantra> {
        delay(150)
        return allMantras.filter { deity == null || it.deity == deity }
    }

    override suspend fun statuses(deity: Deity?): List<WhatsAppStatus> {
        delay(150)
        return allStatuses.filter { deity == null || it.deity == deity }
    }

    override suspend fun festivals(): List<Festival> {
        delay(100)
        return allFestivals
    }

    override suspend fun featuredWallpaper(): Wallpaper =
        allWallpapers.first { it.featured }.let { pickForToday(allWallpapers.filter { w -> w.featured }, it) }

    override suspend fun mantraOfTheDay(): Mantra = pickForToday(allMantras, allMantras.first())

    override suspend fun bhajanOfTheDay(): Bhajan = pickForToday(allBhajans, allBhajans.first())

    override suspend fun statusOfTheDay(): WhatsAppStatus = pickForToday(allStatuses, allStatuses.first())

    override suspend fun byId(type: ContentType, id: String): DevotionalContent? = when (type) {
        ContentType.WALLPAPER -> allWallpapers.firstOrNull { it.id == id }
        ContentType.BHAJAN -> allBhajans.firstOrNull { it.id == id }
        ContentType.MANTRA -> allMantras.firstOrNull { it.id == id }
        ContentType.STATUS -> allStatuses.firstOrNull { it.id == id }
    }

    override suspend fun search(query: String): SearchResults {
        delay(120)
        val q = query.trim()
        if (q.isEmpty()) {
            return SearchResults(
                wallpapers = allWallpapers.filter { it.featured },
                mantras = allMantras.take(5),
                statuses = allStatuses.sortedByDescending { it.shareCount }.take(5)
            )
        }
        fun titleOrDeityMatches(title: String, deity: Deity, tags: List<String> = emptyList()): Boolean {
            val lower = q.lowercase()
            return title.lowercase().contains(lower) ||
                tags.any { it.lowercase().contains(lower) } ||
                Deity.matches(q, deity)
        }
        return SearchResults(
            wallpapers = allWallpapers.filter { titleOrDeityMatches(it.title, it.deity, it.tags) },
            mantras = allMantras.filter { titleOrDeityMatches(it.title, it.deity, listOf(it.purpose, it.category)) },
            statuses = allStatuses.filter { titleOrDeityMatches(it.title, it.deity, listOf(it.category)) }
        )
    }

    /** Deterministic "content of the day" so it's stable within a calendar day. */
    private fun <T> pickForToday(list: List<T>, fallback: T): T {
        if (list.isEmpty()) return fallback
        val dayOfYear = java.time.LocalDate.now().dayOfYear
        return list[dayOfYear % list.size]
    }

    override suspend fun deityPortraits(): Map<Deity, DeityPortrait> =
        Deity.entries.associateWith { DeityPortrait() }

    override suspend fun bellSoundUrl(): String? = null
    override suspend fun subscriptionPlans(): List<SubscriptionPlan> = SubscriptionPlan.ALL
}
