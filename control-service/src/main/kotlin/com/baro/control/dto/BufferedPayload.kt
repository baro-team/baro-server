package com.baro.control.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class BufferedPayload(
    @JsonProperty("vehicle_id") val vehicleId: Any = "",
    val buffered: List<TelemetryPayload> = emptyList(),
)
