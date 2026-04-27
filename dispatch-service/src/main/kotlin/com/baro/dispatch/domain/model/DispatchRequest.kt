package com.baro.dispatch.domain.model

import java.time.OffsetDateTime

data class DispatchRequest(
    val userId: Long,
    val origin: GeoPoint,
    val destination: GeoPoint,
    val status: DispatchRequestStatus,
    val requestedAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
) {
    init {
        require(userId > 0) { "사용자 ID는 양수여야 합니다." }
    }

    companion object {
        fun pending(userId: Long, origin: GeoPoint, destination: GeoPoint, now: OffsetDateTime): DispatchRequest =
            DispatchRequest(
                userId = userId,
                origin = origin,
                destination = destination,
                status = DispatchRequestStatus.PENDING,
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
