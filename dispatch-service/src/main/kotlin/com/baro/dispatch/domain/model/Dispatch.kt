package com.baro.dispatch.domain.model

import java.time.OffsetDateTime

data class Dispatch(
    val requestId: Long,
    val userId: Long,
    val carId: Long,
    val standId: Long,
    val createdAt: OffsetDateTime,
    val estimatedPickupTime: Int,
    val estimatedRideTime: Int,
    val pickupRoutePath: List<GeoPoint>,
    val dropoffRoutePath: List<GeoPoint>,
    val fare: Int,
    val status: DispatchStatus,
) {
    init {
        require(requestId > 0) { "PRE배차 요청 ID는 양수여야 합니다." }
        require(userId > 0) { "사용자 ID는 양수여야 합니다." }
        require(carId >= 0) { "차량 ID는 0 이상이어야 합니다." }
        require(standId >= 0) { "승차장 ID는 0 이상이어야 합니다." }
        require(estimatedPickupTime >= 0) { "예상 픽업 시간은 0 이상이어야 합니다." }
        require(estimatedRideTime >= 0) { "예상 운행 시간은 0 이상이어야 합니다." }
        require(fare >= 0) { "요금은 0 이상이어야 합니다." }
    }

    companion object {
        fun requested(
            requestId: Long,
            userId: Long,
            carId: Long,
            standId: Long,
            createdAt: OffsetDateTime,
            estimatedPickupTime: Int,
            estimatedRideTime: Int,
            pickupRoutePath: List<GeoPoint>,
            dropoffRoutePath: List<GeoPoint>,
            fare: Int,
        ): Dispatch =
            Dispatch(
                requestId = requestId,
                userId = userId,
                carId = carId,
                standId = standId,
                createdAt = createdAt,
                estimatedPickupTime = estimatedPickupTime,
                estimatedRideTime = estimatedRideTime,
                pickupRoutePath = pickupRoutePath,
                dropoffRoutePath = dropoffRoutePath,
                fare = fare,
                status = DispatchStatus.REQUESTED,
            )
    }
}

enum class DispatchStatus {
    REQUESTED,
    ASSIGNED,
    CANCELLED,
    COMPLETED,
}
