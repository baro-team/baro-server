package com.baro.control.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class CommandRequest(
    val type: String,
    val tripId: String? = null,
    val route: List<Map<String, Double>>? = null,
    val phase: String? = null,
    val distanceM: Int? = null,
    val durationS: Int? = null,
)
