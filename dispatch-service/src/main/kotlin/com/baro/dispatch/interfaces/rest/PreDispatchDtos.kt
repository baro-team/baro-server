package com.baro.dispatch.interfaces.rest

import com.baro.dispatch.application.service.PreDispatchCommand
import com.baro.dispatch.application.service.PreDispatchResult
import com.baro.dispatch.domain.model.GeoPoint
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class PreDispatchRequest(
    @field:Schema(description = "배차 요청 사용자 ID", example = "1001")
    val userId: Long,
    @field:Schema(description = "출발지 정보")
    val origin: LocationPointRequest,
    @field:Schema(description = "도착지 정보")
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
    @field:Schema(description = "위도", example = "37.547")
    val lat: Double,
    @field:Schema(description = "경도", example = "127.091896")
    val lon: Double,
    @field:Schema(description = "장소명", example = "건국대학교")
    val name: String? = null,
) {
    fun toGeoPoint(): GeoPoint = GeoPoint(longitude = lon, latitude = lat, name = name)
}

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class PreDispatchResponse(
    @field:Schema(description = "요청 ID", example = "1")
    val requestId: Long,
    @field:Schema(description = "예상 요금", example = "18500")
    val fare: Int,
    @field:Schema(
        description = "경로 좌표 목록 [경도, 위도]",
        example = "[[127.091896,37.547],[126.925011,37.551464]]",
    )
    val routePath: List<List<Double>>,
    @field:Schema(description = "예상 소요 시간(초)", example = "2140")
    val estimatedTime: Int,
    @field:Schema(description = "거리(km)", example = "15.8")
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
