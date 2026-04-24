package com.baro.control.dto

data class VehicleStatus(
    val vehicleId: String,
    val latitude: Double?,
    val longitude: Double?,
    val speed: Int?,
    val heading: Double?,
    val battery: Double?,
    val autonomyMode: String?,
    val status: String?,
    val tripId: String?,
    val lastSeen: String?,
)
