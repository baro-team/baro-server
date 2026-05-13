package com.baro.relocation

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RelocationServiceApplication

fun main(args: Array<String>) {
    runApplication<RelocationServiceApplication>(*args)
}
