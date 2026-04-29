package com.baro.control.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class AckPayload(
    val vehicleId: Any = "",
    val commandType: String = "",
    val tripId: String = "",
)
