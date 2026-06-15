package com.baro.dispatch.application.port.out

interface DispatchableCarProjection {
    fun saveIdleCarLocation(carId: Long, carNumber: String?, latitude: Double, longitude: Double)
    fun removeCar(carId: Long)
    fun findNearestIdleCar(latitude: Double, longitude: Double): DispatchableCarCandidate?
}

data class DispatchableCarCandidate(
    val carId: Long,
    val carNumber: String?,
    val distanceKm: Double?,
    val latitude: Double,
    val longitude: Double,
)
