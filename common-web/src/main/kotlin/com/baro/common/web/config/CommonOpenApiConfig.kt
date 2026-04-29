package com.baro.common.web.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@AutoConfiguration
@EnableConfigurationProperties(BaroOpenApiProperties::class)
class CommonOpenApiConfig {
    @Bean
    fun baroOpenApi(properties: BaroOpenApiProperties): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title(properties.title)
                    .description(properties.description)
                    .version(properties.version),
            )
}

@ConfigurationProperties(prefix = "baro.openapi")
data class BaroOpenApiProperties(
    val title: String = "BARO API",
    val description: String = "BARO API documentation",
    val version: String = "v1",
)
