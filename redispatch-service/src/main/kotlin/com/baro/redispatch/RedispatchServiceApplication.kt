package com.baro.redispatch

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RedispatchServiceApplication

fun main(args: Array<String>) {
    runApplication<RedispatchServiceApplication>(*args)
}
