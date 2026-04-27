package com.baro.dispatch

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class DispatchServiceApplication

fun main(args: Array<String>) {
    runApplication<DispatchServiceApplication>(*args)
}

