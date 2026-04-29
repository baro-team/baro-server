package com.baro.dispatch.application.port.out

import com.baro.dispatch.domain.model.GeoPoint

interface DirectionsPort {
    fun findRoute(origin: GeoPoint, destination: GeoPoint): RouteEstimate
}

data class RouteEstimate(
    val fare: Int,
    val routePath: List<GeoPoint>,
    val durationSeconds: Int,
    val distanceMeters: Int,
)
