package com.baro.control.dto

data class VehicleState(
    val vehicleId: String,
    val carNumber: String? = null,
    val latitude: Double,
    val longitude: Double,
    val speed: Int,
    val battery: Int,
    val heading: Double,
    val status: String,
    val timestamp: String,
)
