package com.sanatan.app

import com.sanatan.app.data.model.BirthDetails
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class BirthDetailsTest {

    @Test
    fun birthDetails_roundTripsThroughJson() {
        val original = BirthDetails(
            name = "Test",
            dateIso = "1995-08-14",
            timeIso = "05:30",
            placeName = "Varanasi",
            latitude = 25.3176,
            longitude = 82.9739,
            timeZoneOffsetMinutes = 330
        )
        val json = Json.encodeToString(original)
        val decoded = Json.decodeFromString<BirthDetails>(json)
        assertEquals(original, decoded)
    }
}
