package com.sanatan.app.data.model

// OWNER: Maneesha (content models)
import kotlinx.serialization.Serializable

@Serializable
data class Mantra(
    val id: String,
    val title: String,
    val devanagari: String,
    val transliteration: String,
    val meaning: String,
    val deity: String,
    val recommendedCount: Int = 108,
    val audioAsset: String? = null
)

@Serializable
data class PanchangDay(
    val dateIso: String,
    val tithi: String,
    val vaar: String,
    val nakshatra: String,
    val yoga: String,
    val karana: String,
    val sunrise: String,
    val sunset: String
)
