package com.baro.dispatch.interfaces.rest

import com.baro.dispatch.application.service.PreDispatchCommand
import com.baro.dispatch.application.service.PreDispatchResult
import com.baro.dispatch.domain.model.GeoPoint
import com.fasterxml.jackson.annotation.JsonProperty

data class PreDispatchRequest(
    @JsonProperty("user_id")
    val userId: Long,
    val origin: LocationPointRequest,
    val destination: LocationPointRequest,
) {
    fun toCommand(): PreDispatchCommand =
        PreDispatchCommand(
            userId = userId,
            origin = origin.toGeoPoint(),
            destination = destination.toGeoPoint(),
        )
}

data class LocationPointRequest(
    val x: Double,
    val y: Double,
    val name: String? = null,
) {
    fun toGeoPoint(): GeoPoint = GeoPoint(longitude = x, latitude = y, name = name)
}

data class PreDispatchResponse(
    @JsonProperty("request_id")
    val requestId: Long,
    val fare: Int,
    @JsonProperty("route_path")
    val routePath: List<List<Double>>,
    @JsonProperty("estimated_time")
    val estimatedTime: Int,
    @JsonProperty("distance_km")
    val distanceKm: Double,
) {
    companion object {
        fun from(result: PreDispatchResult): PreDispatchResponse =
            PreDispatchResponse(
                requestId = result.requestId,
                fare = result.fare,
                routePath = result.routePath.map { listOf(it.longitude, it.latitude) },
                estimatedTime = result.estimatedTime,
                distanceKm = result.distanceKm,
            )
    }
}
