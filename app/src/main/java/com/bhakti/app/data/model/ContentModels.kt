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
    val featured: Boolean = false
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
    val status: PublishStatus = PublishStatus.PUBLISHED
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
    val status: PublishStatus = PublishStatus.PUBLISHED
) : DevotionalContent

enum class StatusMediaType { IMAGE, VIDEO, TEXT }

data class WhatsAppStatus(
    override val id: String,
    override val title: String,
    override val deity: Deity,
    val mediaType: StatusMediaType,
    val category: String,
    val caption: String,
    val shareCount: Int = 0,
    val status: PublishStatus = PublishStatus.PUBLISHED
) : DevotionalContent

data class Festival(
    val id: String,
    val name: String,
    val dateIso: String,
    val deity: Deity? = null
)
