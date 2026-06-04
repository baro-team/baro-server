package com.baro.dispatch.application.service

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "dispatch.matching")
data class DispatchMatchingProperties(
    val searchRadiusStepsKm: List<Double> = listOf(5.0, 10.0, 15.0),
) {
    init {
        require(searchRadiusStepsKm.isNotEmpty()) { "배차 차량 검색 반경은 하나 이상이어야 합니다." }
        require(searchRadiusStepsKm.all { it > 0 }) { "배차 차량 검색 반경은 양수여야 합니다." }
    }
}
