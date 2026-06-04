package com.baro.dispatch.application.service

import com.baro.dispatch.infrastructure.kafka.CarStatus
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
open class CarStateService {

    private val log = LoggerFactory.getLogger(javaClass)

    open fun handle(command: CarStateCommand) {
        log.info("차량 상태 데이터를 수신했습니다. carId={}, status={}, timestamp={}", command.carId, command.status.value, command.timestamp)
    }
}

data class CarStateCommand(
    val carIdKey: String?,
    val carId: Long,
    val latitude: Double,
    val longitude: Double,
    val speed: Int,
    val battery: Int,
    val heading: Double,
    val status: CarStatus,
    val timestamp: String,
)
