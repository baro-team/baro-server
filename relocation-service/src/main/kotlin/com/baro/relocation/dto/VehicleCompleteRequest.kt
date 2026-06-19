package com.baro.relocation.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class VehicleCompleteRequest(
    @JsonProperty("car_id") val carId: Long,
    val lat: Double,
    val lon: Double,
)
