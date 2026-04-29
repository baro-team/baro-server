package com.baro.common.web.config

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean

@AutoConfiguration
class CommonJacksonConfig {
    @Bean
    fun snakeCaseObjectMapper(): Jackson2ObjectMapperBuilderCustomizer =
        Jackson2ObjectMapperBuilderCustomizer { builder ->
            builder.propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        }
}
