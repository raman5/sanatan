package com.bhakti.app.data.repository

import com.bhakti.app.data.model.Bhajan
import com.bhakti.app.data.model.ContentType
import com.bhakti.app.data.model.Deity
import com.bhakti.app.data.model.DevotionalContent
import com.bhakti.app.data.model.Festival
import com.bhakti.app.data.model.Mantra
import com.bhakti.app.data.model.StatusMediaType
import com.bhakti.app.data.model.Wallpaper
import com.bhakti.app.data.model.WhatsAppStatus
import kotlinx.coroutines.delay

data class SearchResults(
    val wallpapers: List<Wallpaper>,
    val bhajans: List<Bhajan>,
    val mantras: List<Mantra>,
    val statuses: List<WhatsAppStatus>
) {
    val isEmpty: Boolean
        get() = wallpapers.isEmpty() && bhajans.isEmpty() && mantras.isEmpty() && statuses.isEmpty()
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
                bhajans = allBhajans.sortedByDescending { it.playCount }.take(5),
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
            bhajans = allBhajans.filter { titleOrDeityMatches(it.title, it.deity) },
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
}
