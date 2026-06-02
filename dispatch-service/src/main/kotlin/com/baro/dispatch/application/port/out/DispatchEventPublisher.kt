package com.baro.dispatch.application.port.out

import java.time.OffsetDateTime

interface DispatchEventPublisher {
    fun publish(event: DispatchEvent)
}

data class DispatchEvent(
    val dispatchId: Long,
    val userId: Long,
    val carId: Long,
    val startLatitude: Double,
    val startLongitude: Double,
    val endLatitude: Double,
    val endLongitude: Double,
    val fare: Int,
    val distanceKm: Double,
    val estimatedTime: Int,
    val status: String,
    val requestedAt: OffsetDateTime,
)
