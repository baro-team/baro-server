package com.baro.control.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class EventPayload(
    @JsonProperty("vehicle_id") val vehicleId: Any = "",
    @JsonProperty("event_type") val eventType: String = "",
    val code: String? = null,
    val detail: Map<String, Any>? = null,
    @JsonProperty("trip_id") val tripId: String? = null,
)
