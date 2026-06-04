package com.baro.dispatch.infrastructure.kafka

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue

data class CarStateMessage(
    @field:JsonProperty("car_id")
    val carId: Long,
    val latitude: Double,
    val longitude: Double,
    val speed: Int,
    val battery: Int,
    val heading: Double,
    val status: CarStatus,
    val timestamp: String,
)

enum class CarStatus(@get:JsonValue val value: String) {
    IDLE("idle"),
    MOVING_TO_PICKUP("moving_to_pickup"),
    DRIVING("driving");

    companion object {
        @JvmStatic
        @JsonCreator
        fun from(value: String): CarStatus = entries.firstOrNull { it.value == value }
            ?: throw IllegalArgumentException("지원하지 않는 차량 상태입니다: $value")
    }
}
