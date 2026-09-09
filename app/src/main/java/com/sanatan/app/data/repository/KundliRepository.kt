package com.sanatan.app.data.repository

// OWNER: Raman
import com.sanatan.app.data.model.BirthDetails
import com.sanatan.app.data.model.KundliChart

interface KundliRepository {
    suspend fun calculate(birth: BirthDetails): KundliChart
    suspend fun savedChart(): KundliChart?
    suspend fun save(chart: KundliChart)
}
