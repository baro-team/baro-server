package com.baro.dispatch.domain.model

import java.time.OffsetDateTime

data class DispatchRequest(
    val userId: Long,
    val origin: GeoPoint,
    val destination: GeoPoint,
    val status: DispatchRequestStatus,
    val fare: Int,
    val routePath: List<GeoPoint>,
    val estimatedTime: Int,
    val distanceKm: Double,
    val requestedAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
) {
    init {
        require(userId > 0) { "사용자 ID는 양수여야 합니다." }
        require(fare >= 0) { "예상 요금은 0 이상이어야 합니다." }
        require(estimatedTime >= 0) { "예상 소요 시간은 0 이상이어야 합니다." }
        require(distanceKm >= 0) { "예상 거리는 0 이상이어야 합니다." }
    }

    companion object {
        fun pending(
            userId: Long,
            origin: GeoPoint,
            destination: GeoPoint,
            fare: Int,
            routePath: List<GeoPoint>,
            estimatedTime: Int,
            distanceKm: Double,
            now: OffsetDateTime,
        ): DispatchRequest =
            DispatchRequest(
                userId = userId,
                origin = origin,
                destination = destination,
                status = DispatchRequestStatus.PENDING,
                fare = fare,
                routePath = routePath,
                estimatedTime = estimatedTime,
                distanceKm = distanceKm,
                requestedAt = now,
                updatedAt = now,
            )
    }
}

enum class DispatchRequestStatus(val value: String) {
    PENDING("pending"),
    MATCHED("matched"),
    RIDING("riding"),
    COMPLETED("completed"),
    CANCELLED("cancelled"),
}
