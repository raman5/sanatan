package com.sanatan.app.data.repository

// OWNER: Maneesha
import com.sanatan.app.data.model.Mantra

interface MantraRepository {
    suspend fun all(): List<Mantra>
    suspend fun byId(id: String): Mantra?
    suspend fun mantraOfTheDay(): Mantra?
}
