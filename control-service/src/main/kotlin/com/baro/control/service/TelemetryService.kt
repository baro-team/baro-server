package com.baro.control.service

import com.baro.control.dto.TelemetryPayload
import com.baro.control.dto.VehicleState
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class TelemetryService(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val stateStore: VehicleStateStore,
    @Value("\${kafka.topic.vehicle-data}") private val vehicleDataTopic: String,
) {
    companion object { private val log = LoggerFactory.getLogger(TelemetryService::class.java) }

    fun handleTelemetry(vehicleId: String, p: TelemetryPayload) {
        stateStore.update(
            VehicleState(
                vehicleId = vehicleId,
                latitude  = p.latitude,
                longitude = p.longitude,
                speed     = p.speed,
                battery   = p.battery.toInt(),
                heading   = p.heading,
                status    = p.status,
                timestamp = p.timestamp,
            )
        )

        val carId = vehicleId.toLongOrNull() ?: run {
            log.warn("vehicleId '{}' 를 Long으로 변환할 수 없어 Kafka publish 생략", vehicleId)
            return
        }

        val message = mapOf(
            "car_id"     to carId,
            "latitude"   to p.latitude,
            "longitude"  to p.longitude,
            "speed"      to p.speed,
            "battery"    to p.battery.toInt(),
            "heading"    to p.heading,
            "status"     to p.status,
            "timestamp"  to p.timestamp,
            "car_number" to p.carNumber,
        )
        kafkaTemplate.send(vehicleDataTopic, carId.toString(), message).whenComplete { _, ex ->
            if (ex != null) log.error("Kafka publish 실패 [carId={}]: {}", carId, ex.message)
        }
    }
}
