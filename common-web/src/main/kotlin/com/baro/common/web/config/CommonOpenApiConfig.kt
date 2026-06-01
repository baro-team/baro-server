package com.baro.common.web.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
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
            .components(
                Components()
                    .addSecuritySchemes(
                        BEARER_AUTH_SECURITY_SCHEME_NAME,
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT"),
                    ),
            )
            .info(
                Info()
                    .title(properties.title)
                    .description(properties.description)
                    .version(properties.version),
            )
            .servers(listOf(Server().url(properties.serverUrl)))

    private companion object {
        const val BEARER_AUTH_SECURITY_SCHEME_NAME = "bearerAuth"
    }
}

@ConfigurationProperties(prefix = "baro.openapi")
data class BaroOpenApiProperties(
    val title: String = "BARO API",
    val description: String = "BARO API documentation",
    val version: String = "v1",
    val serverUrl: String = "/",
)
