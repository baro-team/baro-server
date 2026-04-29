package com.baro.common.web.config

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import java.time.Clock

@AutoConfiguration
class CommonTimeConfig {
    @Bean
    fun clock(): Clock = Clock.systemDefaultZone()
}
