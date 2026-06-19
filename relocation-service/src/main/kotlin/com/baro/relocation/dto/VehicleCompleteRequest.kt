package com.baro.relocation.dto

data class VehicleCompleteRequest(
    val carId: Long,
    val lat: Double,
    val lon: Double,
)
