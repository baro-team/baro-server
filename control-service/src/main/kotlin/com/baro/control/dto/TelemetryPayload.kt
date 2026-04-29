package com.baro.control.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class TelemetryPayload(
    val seq: Int = 0,
    val vehicleId: Any = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val speed: Int = 0,
    val heading: Double = 0.0,
    val battery: Double = 0.0,
    val autonomyMode: String = "auto",
    val status: String = "idle",
    val timestamp: String = "",
    val tripId: String? = null,
    val alerts: Map<String, Any>? = null,
)
