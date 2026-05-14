package com.baro.common.security

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "baro.jwt")
data class JwtProperties(
    val issuer: String,
    val secret: String,
    val accessTokenExpirationSeconds: Long = 0,
    val refreshTokenExpirationSeconds: Long = 0,
)
