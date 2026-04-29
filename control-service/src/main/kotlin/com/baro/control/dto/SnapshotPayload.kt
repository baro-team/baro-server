package com.baro.control.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SnapshotPayload(
    val vehicleId: Any = "",
    val battery: Double = 0.0,
    val tirePressure: Map<String, Double> = emptyMap(),
    val engineOil: Double = 0.0,
    val brakeOil: Double = 0.0,
    val washerFluid: Double = 0.0,
    val extTemp: Double = 0.0,
    val timestamp: String = "",
)
