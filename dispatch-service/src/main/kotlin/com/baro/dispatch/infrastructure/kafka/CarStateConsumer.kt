package com.baro.dispatch.infrastructure.kafka

import com.baro.dispatch.application.service.CarStateCommand
import com.baro.dispatch.application.service.CarStateService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class CarStateConsumer(
    private val carStateService: CarStateService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = ["\${kafka.topic.vehicle-data}"],
        groupId = "\${spring.kafka.consumer.group-id}",
        containerFactory = "carStateKafkaListenerContainerFactory",
        autoStartup = "\${kafka.listener.car-state.auto-startup:true}",
    )
    fun consume(message: CarStateMessage) {
        log.info("차량 상태 메시지를 수신했습니다. carId={}, status={}", message.carId, message.status.value)
        try {
            carStateService.handle(message.toCommand())
        } catch (exception: Exception) {
            log.warn("차량 상태 메시지 처리에 실패했습니다. carId={}", message.carId, exception)
        }
    }

    private fun CarStateMessage.toCommand() = CarStateCommand(
        carId = carId,
        carNumber = carNumber,
        latitude = latitude,
        longitude = longitude,
        speed = speed,
        battery = battery,
        heading = heading,
        status = status,
        timestamp = timestamp,
    )
}
