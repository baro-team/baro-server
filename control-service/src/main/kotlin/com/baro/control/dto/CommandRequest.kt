package com.baro.control.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class CommandRequest(
    val type: String,
    @JsonProperty("trip_id") val tripId: String? = null,
    val route: List<Map<String, Double>>? = null,
    val phase: String? = null,
    @JsonProperty("distance_m") val distanceM: Int? = null,
    @JsonProperty("duration_s") val durationS: Int? = null,
)
