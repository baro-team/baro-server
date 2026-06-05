package com.baro.dispatch.infrastructure.redis

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "dispatch.redis")
data class DispatchRedisProperties(
    val idleCarGeoKey: String = "dispatch:cars:idle:geo",
    val idleCarSearchRadiusKm: Double = 5.0,
)
