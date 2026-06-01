package com.baro.common.web.config

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@AutoConfiguration
@EnableConfigurationProperties(BaroCorsProperties::class)
class CommonCorsConfig(
    private val properties: BaroCorsProperties,
) : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOriginPatterns(*properties.allowedOriginPatterns.toTypedArray())
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .exposedHeaders("Location")
    }
}

@ConfigurationProperties(prefix = "baro.cors")
data class BaroCorsProperties(
    val allowedOriginPatterns: List<String> = listOf(
        "https://dev.barocloud.com",
        "http://localhost:*",
    ),
)
