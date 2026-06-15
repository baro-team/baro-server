package com.baro.user.infrastructure.security

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "baro.jwt")
data class JwtProperties(
    val secret: String,
    val accessTokenExpirationSeconds: Long = 0,
    val refreshTokenExpirationSeconds: Long = 0,
)
