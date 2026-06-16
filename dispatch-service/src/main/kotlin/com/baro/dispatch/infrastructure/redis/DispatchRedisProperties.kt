package com.baro.dispatch.infrastructure.redis

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "dispatch.redis")
data class DispatchRedisProperties(
    val idleCarGeoKey: String = "dispatch:cars:idle:geo",
    val idleCarSearchRadiusKm: Double = 5.0,
    val idleCarSearchRadiiKm: List<Double> = emptyList(),
    val idleCarMaxCandidates: Int = 10,
    val stalenessThresholdSeconds: Long = 60L,
    val reservationKeyPrefix: String = "dispatch:vehicle:reservation:",
    val reservationTtlSeconds: Long = 30L,
)
