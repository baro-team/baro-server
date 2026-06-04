package com.baro.control

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class ControlServiceApplication

fun main(args: Array<String>) {
    runApplication<ControlServiceApplication>(*args)
}
