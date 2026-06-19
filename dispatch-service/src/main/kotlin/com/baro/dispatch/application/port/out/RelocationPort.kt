package com.baro.dispatch.application.port.out

data class RelocationDestination(
    val targetStandId: String,
    val targetLat: Double,
    val targetLon: Double,
)

interface RelocationPort {
    fun assignRelocation(carId: Long, currentLat: Double, currentLon: Double): RelocationDestination
}
