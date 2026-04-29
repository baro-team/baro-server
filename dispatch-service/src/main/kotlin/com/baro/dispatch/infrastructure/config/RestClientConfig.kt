package com.baro.dispatch.infrastructure.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
class RestClientConfig {
    @Bean
    fun clock(): Clock = Clock.systemDefaultZone()
}
