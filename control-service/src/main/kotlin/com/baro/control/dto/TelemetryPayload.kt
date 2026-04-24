package com.baro.control.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TelemetryPayload(
    val seq: Int = 0,
    @JsonProperty("vehicle_id") val vehicleId: Any = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val speed: Int = 0,
    val heading: Double = 0.0,
    val battery: Double = 0.0,
    @JsonProperty("autonomy_mode") val autonomyMode: String = "auto",
    val status: String = "idle",
    val timestamp: String = "",
    @JsonProperty("trip_id") val tripId: String? = null,
    val alerts: Map<String, Any>? = null,
)
