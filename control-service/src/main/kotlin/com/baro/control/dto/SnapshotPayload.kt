package com.baro.control.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class SnapshotPayload(
    @JsonProperty("vehicle_id") val vehicleId: Any = "",
    val battery: Double = 0.0,
    @JsonProperty("tire_pressure") val tirePressure: Map<String, Double> = emptyMap(),
    @JsonProperty("engine_oil") val engineOil: Double = 0.0,
    @JsonProperty("brake_oil") val brakeOil: Double = 0.0,
    @JsonProperty("washer_fluid") val washerFluid: Double = 0.0,
    @JsonProperty("ext_temp") val extTemp: Double = 0.0,
    val timestamp: String = "",
)
