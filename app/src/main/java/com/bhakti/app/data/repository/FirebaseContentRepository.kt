package com.bhakti.app.data.repository

import android.content.Context
import com.bhakti.app.core.firebase.FirebaseConfig
import com.bhakti.app.data.model.Bhajan
import com.bhakti.app.data.model.BillingCycle
import com.bhakti.app.data.model.ContentType
import com.bhakti.app.data.model.Deity
import com.bhakti.app.data.model.DeityPortrait
import com.bhakti.app.data.model.DevotionalContent
import com.bhakti.app.data.model.Festival
import com.bhakti.app.data.model.Mantra
import com.bhakti.app.data.model.PublishStatus
import com.bhakti.app.data.model.StatusMediaType
import com.bhakti.app.data.model.SubscriptionPlan
import com.bhakti.app.data.model.Wallpaper
import com.bhakti.app.data.model.WhatsAppStatus
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ConcurrentHashMap

/**
 * Live content backed by Firestore (structured fields) + Cloud Storage
 * (image/audio URLs, written into those same fields by the migration
 * script - see backend/migrate-content.js). Collections mirror
 * [SampleContent]'s shape 1:1 so the same ids keep working end to end.
 *
 * Mapping is manual (not Firestore's reflection-based toObject()) because
 * several fields here have no default value, which that reflection path
 * requires - manual mapping is more verbose but fails loudly and
 * predictably on a malformed document instead of silently.
 *
 * Only used when [FirebaseConfig.isAvailable] - see [com.bhakti.app.core.di.AppContainer].
 */
class FirebaseContentRepository(context: Context) : ContentRepository {

    private val db: FirebaseFirestore = FirebaseConfig.firestore(context.applicationContext)

    // Suffixed "Collection" throughout so none of these shadow/clash with
    // the same-named override functions below (wallpapers(), bhajans(), ...).
    private val wallpapersCollection = db.collection("wallpapers")
    private val bhajansCollection = db.collection("bhajans")
    private val mantrasCollection = db.collection("mantras")
    private val statusesCollection = db.collection("statuses")
    private val festivalsCollection = db.collection("festivals")
    private val deityPortraitsCollection = db.collection("deityPortraits")
    private val appAssets = db.collection("appAssets")
    private val subscriptionPlansCollection = db.collection("subscriptionPlans")

    // --- Caching -----------------------------------------------------------
    //
    // The whole catalogue is small (tens of documents), so each collection is
    // fetched once per app session and filtered in memory - every screen
    // after the first reads from RAM instead of making its own Firestore
    // round-trip. Within that one fetch, Firestore's on-phone cache is read
    // first (instant) and refreshed from the server in the background, so
    // content edits made in the Firebase console show up from the next app
    // launch onwards without ever making a screen wait on the network.

    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val sessionCache = ConcurrentHashMap<String, Deferred<Any>>()

    @Suppress("UNCHECKED_CAST")
    private suspend fun <T : Any> cached(key: String, load: suspend () -> T): T {
        val deferred = sessionCache.computeIfAbsent(key) { backgroundScope.async { load() } }
        return try {
            deferred.await() as T
        } catch (e: Exception) {
            sessionCache.remove(key, deferred) // don't pin a failure - retry next call
            throw e
        }
    }

    /** Cache-first read of a whole collection, with a background server refresh when served from cache. */
    private suspend fun documentsOf(ref: CollectionReference): List<DocumentSnapshot> {
        val local = runCatching { ref.get(Source.CACHE).await() }.getOrNull()
        if (local != null && !local.isEmpty) {
            backgroundScope.launch { runCatching { ref.get(Source.SERVER).await() } }
            return local.documents
        }
        return ref.get().await().documents
    }

    private suspend fun allWallpapers(): List<Wallpaper> =
        cached("wallpapers") { documentsOf(wallpapersCollection).mapNotNull { it.toWallpaper() } }
    private suspend fun allBhajans(): List<Bhajan> =
        cached("bhajans") { documentsOf(bhajansCollection).mapNotNull { it.toBhajan() } }
    private suspend fun allMantras(): List<Mantra> =
        cached("mantras") { documentsOf(mantrasCollection).mapNotNull { it.toMantra() } }
    private suspend fun allStatuses(): List<WhatsAppStatus> =
        cached("statuses") { documentsOf(statusesCollection).mapNotNull { it.toWhatsAppStatus() } }

    override suspend fun wallpapers(deity: Deity?): List<Wallpaper> =
        allWallpapers().filter { deity == null || it.deity == deity }

    override suspend fun bhajans(deity: Deity?): List<Bhajan> =
        allBhajans().filter { deity == null || it.deity == deity }

    override suspend fun mantras(deity: Deity?): List<Mantra> =
        allMantras().filter { deity == null || it.deity == deity }

    override suspend fun statuses(deity: Deity?): List<WhatsAppStatus> =
        allStatuses().filter { deity == null || it.deity == deity }

    override suspend fun festivals(): List<Festival> =
        cached("festivals") { documentsOf(festivalsCollection).mapNotNull { it.toFestival() } }

    override suspend fun featuredWallpaper(): Wallpaper {
        val all = allWallpapers()
        return pickForToday(all.filter { it.featured }, fallback = all.first())
    }

    override suspend fun mantraOfTheDay(): Mantra = allMantras().let { pickForToday(it, it.first()) }
    override suspend fun bhajanOfTheDay(): Bhajan = allBhajans().let { pickForToday(it, it.first()) }
    override suspend fun statusOfTheDay(): WhatsAppStatus = allStatuses().let { pickForToday(it, it.first()) }

    override suspend fun byId(type: ContentType, id: String): DevotionalContent? = when (type) {
        ContentType.WALLPAPER -> allWallpapers().firstOrNull { it.id == id }
        ContentType.BHAJAN -> allBhajans().firstOrNull { it.id == id }
        ContentType.MANTRA -> allMantras().firstOrNull { it.id == id }
        ContentType.STATUS -> allStatuses().firstOrNull { it.id == id }
    }

    override suspend fun search(query: String): SearchResults {
        val q = query.trim()
        if (q.isEmpty()) {
            return SearchResults(
                wallpapers = allWallpapers().filter { it.featured },
                mantras = allMantras().take(5),
                statuses = allStatuses().sortedByDescending { it.shareCount }.take(5)
            )
        }
        // Firestore has no full-text search - filter the (small) catalogue client-side,
        // same as FakeContentRepository. A catalogue large enough for this to matter
        // would call for Algolia/Typesense fed from this same Firestore data, not a
        // change to this method's contract.
        fun titleOrDeityMatches(title: String, deity: Deity, tags: List<String> = emptyList()): Boolean {
            val lower = q.lowercase()
            return title.lowercase().contains(lower) ||
                tags.any { it.lowercase().contains(lower) } ||
                Deity.matches(q, deity)
        }
        return SearchResults(
            wallpapers = allWallpapers().filter { titleOrDeityMatches(it.title, it.deity, it.tags) },
            mantras = allMantras().filter { titleOrDeityMatches(it.title, it.deity, listOf(it.purpose, it.category)) },
            statuses = allStatuses().filter { titleOrDeityMatches(it.title, it.deity, listOf(it.category)) }
        )
    }

    override suspend fun deityPortraits(): Map<Deity, DeityPortrait> = cached("deityPortraits") {
        documentsOf(deityPortraitsCollection).mapNotNull { doc ->
            val deity = Deity.entries.firstOrNull { it.name.lowercase() == doc.id } ?: return@mapNotNull null
            deity to DeityPortrait(
                primaryUrl = doc.getString("portraitUrl"),
                secondaryUrl = doc.getString("secondaryPortraitUrl"),
                naamJapaAudioUrl = doc.getString("naamJapaAudioUrl")
            )
        }.toMap()
    }

    override suspend fun bellSoundUrl(): String? = cached("appAssets") {
        documentsOf(appAssets).associate { it.id to it.getString("audioUrl") }
    }["bell"]

    override suspend fun subscriptionPlans(): List<SubscriptionPlan> = cached("subscriptionPlans") {
        documentsOf(subscriptionPlansCollection).mapNotNull { it.toSubscriptionPlan() }.ifEmpty { SubscriptionPlan.ALL }
    }

    private fun <T> pickForToday(list: List<T>, fallback: T): T {
        if (list.isEmpty()) return fallback
        val dayOfYear = java.time.LocalDate.now().dayOfYear
        return list[dayOfYear % list.size]
    }

    // --- Document -> domain model mapping -----------------------------------

    private fun DocumentSnapshot.toWallpaper(): Wallpaper? {
        val deity = getDeity() ?: return null
        return Wallpaper(
            id = id,
            title = getString("title") ?: return null,
            deity = deity,
            category = getString("category") ?: "",
            theme = getString("theme") ?: "",
            festival = getString("festival"),
            style = getString("style") ?: "",
            tags = (get("tags") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            resolution = getString("resolution") ?: "1080x1920",
            language = getString("language") ?: "Hindi",
            status = getPublishStatus(),
            featured = getBoolean("featured") ?: false,
            imageVariant = (getLong("imageVariant") ?: 0L).toInt(),
            imageUrl = getString("imageUrl")
        )
    }

    private fun DocumentSnapshot.toBhajan(): Bhajan? {
        val deity = getDeity() ?: return null
        return Bhajan(
            id = id,
            title = getString("title") ?: return null,
            deity = deity,
            category = getString("category") ?: "",
            singer = getString("singer"),
            durationSec = (getLong("durationSec") ?: 0L).toInt(),
            playCount = (getLong("playCount") ?: 0L).toInt(),
            featured = getBoolean("featured") ?: false,
            status = getPublishStatus(),
            audioUrl = getString("audioUrl")
        )
    }

    private fun DocumentSnapshot.toMantra(): Mantra? {
        val deity = getDeity() ?: return null
        return Mantra(
            id = id,
            title = getString("title") ?: return null,
            deity = deity,
            purpose = getString("purpose") ?: "",
            category = getString("category") ?: "",
            devanagari = getString("devanagari") ?: "",
            transliteration = getString("transliteration") ?: "",
            meaning = getString("meaning"),
            recommendedCount = (getLong("recommendedCount") ?: 108L).toInt(),
            hasAudio = getBoolean("hasAudio") ?: true,
            durationSec = (getLong("durationSec") ?: 90L).toInt(),
            status = getPublishStatus(),
            audioUrl = getString("audioUrl"),
            japaAudioUrl = getString("japaAudioUrl")
        )
    }

    private fun DocumentSnapshot.toWhatsAppStatus(): WhatsAppStatus? {
        val deity = getDeity() ?: return null
        val mediaType = getString("mediaType")?.let { raw ->
            StatusMediaType.entries.firstOrNull { it.name == raw }
        } ?: StatusMediaType.IMAGE
        return WhatsAppStatus(
            id = id,
            title = getString("title") ?: return null,
            deity = deity,
            mediaType = mediaType,
            category = getString("category") ?: "",
            caption = getString("caption") ?: "",
            greeting = getString("greeting") ?: "",
            shloka = getString("shloka"),
            shareCount = (getLong("shareCount") ?: 0L).toInt(),
            status = getPublishStatus(),
            imageVariant = (getLong("imageVariant") ?: 0L).toInt(),
            imageUrl = getString("imageUrl")
        )
    }

    private fun DocumentSnapshot.toFestival(): Festival? {
        return Festival(
            id = id,
            name = getString("name") ?: return null,
            dateIso = getString("dateIso") ?: return null,
            deity = getDeity()
        )
    }

    private fun DocumentSnapshot.toSubscriptionPlan(): SubscriptionPlan? {
        val cycle = getString("cycle")?.let { raw -> BillingCycle.entries.firstOrNull { it.name == raw } } ?: return null
        return SubscriptionPlan(
            id = id,
            cycle = cycle,
            label = getString("label") ?: return null,
            priceRupees = (getLong("priceRupees") ?: return null).toInt(),
            perMonthEquivalent = (getLong("perMonthEquivalent") ?: 0L).toInt(),
            badge = getString("badge")
        )
    }

    private fun DocumentSnapshot.getDeity(): Deity? =
        getString("deity")?.let { raw -> Deity.entries.firstOrNull { it.name == raw } }

    private fun DocumentSnapshot.getPublishStatus(): PublishStatus =
        getString("status")?.let { raw -> PublishStatus.entries.firstOrNull { it.name == raw } } ?: PublishStatus.PUBLISHED
}
