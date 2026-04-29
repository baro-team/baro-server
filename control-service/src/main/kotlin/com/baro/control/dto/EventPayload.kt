package com.baro.control.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class EventPayload(
    val vehicleId: Any = "",
    val eventType: String = "",
    val code: String? = null,
    val detail: Map<String, Any>? = null,
    val tripId: String? = null,
)
