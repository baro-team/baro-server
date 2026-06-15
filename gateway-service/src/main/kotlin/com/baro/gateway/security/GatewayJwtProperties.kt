package com.baro.gateway.security

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "baro.jwt")
data class GatewayJwtProperties(
    val secret: String = "",
)
