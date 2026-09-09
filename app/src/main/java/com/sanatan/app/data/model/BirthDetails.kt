package com.sanatan.app.data.model

// OWNER: Raman (data layer)
import kotlinx.serialization.Serializable

@Serializable
data class BirthDetails(
    val name: String,
    val dateIso: String,      // yyyy-MM-dd
    val timeIso: String,      // HH:mm, local time at place of birth
    val placeName: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneOffsetMinutes: Int
)

@Serializable
data class PlanetPosition(
    val planet: String,
    val signIndex: Int,       // 0 = Mesha .. 11 = Meena
    val degreeInSign: Double,
    val nakshatra: String,
    val isRetrograde: Boolean = false
)

@Serializable
data class KundliChart(
    val birth: BirthDetails,
    val lagnaSignIndex: Int,
    val planets: List<PlanetPosition>
)
