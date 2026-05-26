package com.baro.dispatch.interfaces.rest

import com.baro.dispatch.application.service.ConfirmDispatchCommand
import com.baro.dispatch.application.service.ConfirmDispatchResult
import io.swagger.v3.oas.annotations.media.Schema

data class ConfirmDispatchRequest(
    @field:Schema(description = "PRE배차 요청 ID", example = "1")
    val requestId: Long,
    @field:Schema(description = "배차 요청 사용자 ID", example = "1001")
    val userId: Long,
) {
    fun toCommand(): ConfirmDispatchCommand = ConfirmDispatchCommand(requestId = requestId, userId = userId)
}

data class ConfirmDispatchResponse(
    @field:Schema(description = "배차 ID", example = "1")
    val dispatchId: Long,
    @field:Schema(description = "PRE배차 요청 ID", example = "1")
    val requestId: Long,
    @field:Schema(description = "배차 요청 사용자 ID", example = "1001")
    val userId: Long,
    @field:Schema(description = "임시 차량 ID", example = "0")
    val carId: Long,
    @field:Schema(description = "임시 승차장 ID", example = "0")
    val standId: Long,
    @field:Schema(description = "예상 픽업 시간(분)", example = "0")
    val estimatedPickupTime: Int,
    @field:Schema(description = "예상 운행 시간(분)", example = "46")
    val estimatedRideTime: Int,
    @field:Schema(description = "픽업 경로 좌표 목록 [경도, 위도]")
    val pickupRoutePath: List<List<Double>>,
    @field:Schema(description = "목적지 경로 좌표 목록 [경도, 위도]")
    val dropoffRoutePath: List<List<Double>>,
    @field:Schema(description = "요금", example = "12100")
    val fare: Int,
    @field:Schema(description = "배차 상태", example = "REQUESTED")
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
