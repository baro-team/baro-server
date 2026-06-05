package com.baro.control.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class AckPayload(
    @JsonProperty("vehicle_id") val vehicleId: String = "",
    @JsonProperty("command_type") val commandType: String = "",
    @JsonProperty("trip_id") val tripId: String = "",
)
