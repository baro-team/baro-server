package com.baro.dispatch.infrastructure.kafka

import com.baro.dispatch.application.service.CarStateCommand
import com.baro.dispatch.application.service.CarStateService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component

private const val MAX_MESSAGE_LAG_MS = 10_000L  // 10초 이상 오래된 메시지 무시

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
    fun consume(
        message: CarStateMessage,
        @Header(name = KafkaHeaders.RECEIVED_KEY, required = false) carIdKey: String?,
        @Header(name = KafkaHeaders.RECEIVED_TIMESTAMP, required = false) producedAt: Long? = null,
    ) {
        if (producedAt != null && producedAt > 0) {
            val lagMs = System.currentTimeMillis() - producedAt
            if (lagMs > MAX_MESSAGE_LAG_MS) {
                log.warn("랙 초과 메시지 무시: carId={}, lag={}ms", message.carId, lagMs)
                return
            }
        }
        log.debug("차량 상태 메시지를 수신했습니다. carId={}, key={}, status={}", message.carId, carIdKey, message.status.value)
        try {
            carStateService.handle(message.toCommand(carIdKey))
        } catch (exception: Exception) {
            log.warn("차량 상태 메시지 처리에 실패했습니다. carId={}", message.carId, exception)
        }
    }

    private fun CarStateMessage.toCommand(carIdKey: String?) = CarStateCommand(
        carIdKey = carIdKey,
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
