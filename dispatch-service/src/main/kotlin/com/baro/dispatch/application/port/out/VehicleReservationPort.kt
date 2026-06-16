package com.baro.dispatch.application.port.out

interface VehicleReservationPort {
    fun reserve(carId: Long, ownerId: String): Boolean
    fun release(carId: Long, ownerId: String)
}
