package com.baro.dispatch.application.port.out

interface DispatchableCarProjection {
    fun saveIdleCarLocation(carId: Long, carNumber: String?, latitude: Double, longitude: Double)
    fun removeCar(carId: Long)
    fun findNearestIdleCars(latitude: Double, longitude: Double): List<DispatchableCarCandidate>
    fun findNearestIdleCar(latitude: Double, longitude: Double): DispatchableCarCandidate? =
        findNearestIdleCars(latitude, longitude).firstOrNull()
}

data class DispatchableCarCandidate(
    val carId: Long,
    val carNumber: String?,
    val distanceKm: Double?,
    val latitude: Double,
    val longitude: Double,
)
