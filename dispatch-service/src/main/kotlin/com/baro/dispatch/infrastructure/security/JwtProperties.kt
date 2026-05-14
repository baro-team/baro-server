package com.baro.dispatch.infrastructure.security

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "baro.jwt")
data class JwtProperties(
    val issuer: String,
    val secret: String,
)
