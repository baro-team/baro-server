package com.baro.dispatch.application.port.out

interface DispatchableCarProjection {
    fun saveIdleCarLocation(carId: Long, latitude: Double, longitude: Double)
    fun removeCar(carId: Long)
}
