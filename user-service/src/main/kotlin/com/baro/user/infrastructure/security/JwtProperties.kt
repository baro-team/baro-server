package com.baro.user.infrastructure.security

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "baro.jwt")
data class JwtProperties(
    val issuer: String,
    val accessTokenExpirationSeconds: Long,
    val refreshTokenExpirationSeconds: Long,
    val secret: String,
)
