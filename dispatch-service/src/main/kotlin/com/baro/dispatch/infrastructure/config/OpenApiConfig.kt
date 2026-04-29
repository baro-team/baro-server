package com.baro.dispatch.infrastructure.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun dispatchOpenApi(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title("Dispatch Service API")
                    .description("BARO dispatch-service API documentation")
                    .version("v1"),
            )
}

