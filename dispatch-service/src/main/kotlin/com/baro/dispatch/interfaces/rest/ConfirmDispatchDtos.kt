package com.baro.dispatch.interfaces.rest

import com.baro.dispatch.application.service.ConfirmDispatchCommand
import com.baro.dispatch.application.service.ConfirmDispatchResult
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class ConfirmDispatchRequest(
    @field:JsonProperty("request_id")
    @field:Schema(name = "request_id", description = "PRE배차 요청 ID", example = "1")
    val requestId: Long,
    @field:JsonProperty("user_id")
    @field:Schema(name = "user_id", description = "배차 요청 사용자 ID", example = "1001")
    val userId: Long,
) {
    fun toCommand(): ConfirmDispatchCommand = ConfirmDispatchCommand(requestId = requestId, userId = userId)
}

data class ConfirmDispatchResponse(
    @field:JsonProperty("dispatch_id")
    @field:Schema(name = "dispatch_id", description = "배차 ID", example = "1")
    val dispatchId: Long,
    @field:JsonProperty("request_id")
    @field:Schema(name = "request_id", description = "PRE배차 요청 ID", example = "1")
    val requestId: Long,
    @field:JsonProperty("user_id")
    @field:Schema(name = "user_id", description = "배차 요청 사용자 ID", example = "1001")
    val userId: Long,
    @field:JsonProperty("car_id")
    @field:Schema(name = "car_id", description = "임시 차량 ID", example = "0")
    val carId: Long,
    @field:JsonProperty("stand_id")
    @field:Schema(name = "stand_id", description = "임시 승차장 ID", example = "0")
    val standId: Long,
    @field:JsonProperty("estimated_pickup_time")
    @field:Schema(name = "estimated_pickup_time", description = "예상 픽업 시간(분)", example = "0")
    val estimatedPickupTime: Int,
    @field:JsonProperty("estimated_ride_time")
    @field:Schema(name = "estimated_ride_time", description = "예상 운행 시간(분)", example = "46")
    val estimatedRideTime: Int,
    @field:JsonProperty("pickup_route_path")
    @field:Schema(name = "pickup_route_path", description = "픽업 경로 좌표 목록 [경도, 위도]")
    val pickupRoutePath: List<List<Double>>,
    @field:JsonProperty("dropoff_route_path")
    @field:Schema(name = "dropoff_route_path", description = "목적지 경로 좌표 목록 [경도, 위도]")
    val dropoffRoutePath: List<List<Double>>,
    @field:Schema(description = "요금", example = "12100")
    val fare: Int,
    @field:JsonProperty("dispatch_status")
    @field:Schema(name = "dispatch_status", description = "배차 상태", example = "REQUESTED")
    val dispatchStatus: String,
) {
    companion object {
        fun from(result: ConfirmDispatchResult): ConfirmDispatchResponse =
            ConfirmDispatchResponse(
                dispatchId = result.dispatchId,
                requestId = result.requestId,
                userId = result.userId,
                carId = result.carId,
                standId = result.standId,
                estimatedPickupTime = result.estimatedPickupTime,
                estimatedRideTime = result.estimatedRideTime,
                pickupRoutePath = result.pickupRoutePath.map { listOf(it.longitude, it.latitude) },
                dropoffRoutePath = result.dropoffRoutePath.map { listOf(it.longitude, it.latitude) },
                fare = result.fare,
                dispatchStatus = result.status,
            )
    }
}
