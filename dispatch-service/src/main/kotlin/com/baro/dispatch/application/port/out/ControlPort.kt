package com.baro.dispatch.application.port.out

import com.baro.dispatch.domain.model.GeoPoint

interface ControlPort {
    fun sendDispatchCommand(
        carId: Long,
        tripId: String,
        route: List<GeoPoint>,
        distanceMeters: Int,
        durationSeconds: Int,
        phase: String,
    )
}
