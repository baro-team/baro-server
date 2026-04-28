package com.baro.control.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class BufferedPayload(
    val vehicleId: Any = "",
    val buffered: List<TelemetryPayload> = emptyList(),
)
