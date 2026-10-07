package com.bhakti.app.data.model

/** Shared marker for anything that can be favourited, searched or shared. */
sealed interface DevotionalContent {
    val id: String
    val title: String
    val deity: Deity
}

enum class ContentType { WALLPAPER, BHAJAN, MANTRA, STATUS }

enum class PublishStatus { DRAFT, PUBLISHED, UNPUBLISHED }

data class Wallpaper(
    override val id: String,
    override val title: String,
    override val deity: Deity,
    val category: String,
    val theme: String,
    val festival: String? = null,
    val style: String,
    val tags: List<String> = emptyList(),
    val resolution: String = "1080x1920",
    val language: String = "Hindi",
    val status: PublishStatus = PublishStatus.PUBLISHED,
    val featured: Boolean = false,
    val imageVariant: Int = 0,
    /**
     * Backend-hosted artwork URL (Firebase Storage). Null when served from
     * [FakeContentRepository] - callers fall back to the bundled drawable via
     * [com.bhakti.app.core.ui.imageFor] (deity, imageVariant) in that case,
     * so no screen needs to branch on which repository is active.
     */
    val imageUrl: String? = null
) : DevotionalContent

data class Bhajan(
    override val id: String,
    override val title: String,
    override val deity: Deity,
    val category: String,
    val singer: String? = null,
    val durationSec: Int,
    val playCount: Int = 0,
    val featured: Boolean = false,
    val status: PublishStatus = PublishStatus.PUBLISHED,
    /** Backend-hosted audio URL (Firebase Storage). Null falls back to the bundled raw resource. */
    val audioUrl: String? = null
) : DevotionalContent

data class Mantra(
    override val id: String,
    override val title: String,
    override val deity: Deity,
    val purpose: String,
    val category: String,
    val devanagari: String,
    val transliteration: String,
    val meaning: String? = null,
    val recommendedCount: Int = 108,
    val hasAudio: Boolean = true,
    val durationSec: Int = 90,
    val status: PublishStatus = PublishStatus.PUBLISHED,
    /** Backend-hosted audio URL (Firebase Storage). Null falls back to the bundled raw resource. */
    val audioUrl: String? = null,
    /**
     * Optional recorded clip of one mantra repetition, played on every japa
     * counter tap. Null speaks [devanagari] with on-device Hindi TTS instead.
     */
    val japaAudioUrl: String? = null
) : DevotionalContent

enum class StatusMediaType { IMAGE, VIDEO, TEXT }

data class WhatsAppStatus(
    override val id: String,
    override val title: String,
    override val deity: Deity,
    val mediaType: StatusMediaType,
    val category: String,
    val caption: String,
    /** Short bold exclamation overlaid on the artwork, e.g. "जय माँ लक्ष्मी". */
    val greeting: String,
    /** A short, already-vetted spiritual chant shown on some cards - null on others for variety. */
    val shloka: String? = null,
    val shareCount: Int = 0,
    val status: PublishStatus = PublishStatus.PUBLISHED,
    val imageVariant: Int = 0,
    /** Backend-hosted artwork URL (Firebase Storage). Null falls back to the bundled drawable. */
    val imageUrl: String? = null
) : DevotionalContent

data class Festival(
    val id: String,
    val name: String,
    val dateIso: String,
    val deity: Deity? = null
)

/**
 * A deity's own portrait art, standalone - not tied to one [Wallpaper]/
 * [WhatsAppStatus]. Used by the deity-picker grids (Naam Japa, Daily Pooja)
 * and the Home deity row. Null fields fall back to the bundled drawable via
 * [com.bhakti.app.core.ui.imageFor] (variant 0/1 respectively).
 */
data class DeityPortrait(
    val primaryUrl: String? = null,
    val secondaryUrl: String? = null,
    /**
     * Recorded clip of the deity's name, played on every Naam Japa tap.
     * Null falls back to on-device text-to-speech of [naamJapaNameFor] -
     * so this only needs setting if you want a real recorded voice.
     */
    val naamJapaAudioUrl: String? = null
)
