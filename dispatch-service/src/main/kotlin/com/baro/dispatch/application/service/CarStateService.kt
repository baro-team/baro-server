package com.baro.dispatch.application.service

import com.baro.dispatch.application.port.out.DispatchableCarProjection
import com.baro.dispatch.infrastructure.kafka.CarStatus
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
open class CarStateService(
    private val dispatchableCarProjection: DispatchableCarProjection,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    open fun handle(command: CarStateCommand) {
        log.info("차량 상태 데이터를 수신했습니다. carId={}, status={}, timestamp={}", command.carId, command.status.value, command.timestamp)
        when (command.status) {
            CarStatus.IDLE -> {
                log.info("배차 가능한 차량 위치를 Redis GEO에 저장합니다. carId={}, latitude={}, longitude={}", command.carId, command.latitude, command.longitude)
                dispatchableCarProjection.saveIdleCarLocation(command.carId, command.latitude, command.longitude)
            }

            CarStatus.MOVING_TO_PICKUP, CarStatus.DRIVING -> {
                log.info("배차 가능 차량 목록에서 제외합니다. carId={}, status={}", command.carId, command.status.value)
                dispatchableCarProjection.removeCar(command.carId)
            }
        }
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
