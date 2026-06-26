package com.baro.control.service

import com.baro.control.dto.TelemetryPayload
import com.baro.control.dto.VehicleState
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicLong

@Service
class TelemetryService(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val stateStore: VehicleStateStore,
    meterRegistry: MeterRegistry,
    @Value("\${kafka.topic.vehicle-data}") private val vehicleDataTopic: String,
) {
    companion object {
        private val log = LoggerFactory.getLogger(TelemetryService::class.java)
        private val knownStatuses = setOf("idle", "moving_to_pickup", "driving", "relocating")
        private val kafkaFailCount = AtomicLong(0)
        private val kafkaFailLoggedAt = AtomicLong(0)
    }

    private val meterRegistry = meterRegistry
    private fun receivedCounter(status: String) = Counter.builder("baro_control_telemetry_received_total")
        .description("Telemetry messages received")
        .tag("status", status)
        .register(this.meterRegistry)
    private val kafkaPublishedCounter = Counter.builder("baro_control_telemetry_kafka_published_total")
        .description("Telemetry messages published to Kafka")
        .register(meterRegistry)
    private val kafkaPublishFailedCounter = Counter.builder("baro_control_telemetry_kafka_publish_failed_total")
        .description("Telemetry messages failed to publish to Kafka")
        .register(meterRegistry)
    private val invalidVehicleIdCounter = Counter.builder("baro_control_telemetry_invalid_vehicle_id_total")
        .description("Telemetry messages with invalid vehicle id")
        .register(meterRegistry)

    fun handleTelemetry(vehicleId: String, p: TelemetryPayload) {
        val normalizedStatus = p.status.takeIf { it in knownStatuses } ?: "unknown"
        receivedCounter(normalizedStatus).increment()

        val state = VehicleState(
            vehicleId = vehicleId,
            carNumber = p.carNumber,
            latitude = p.latitude,
            longitude = p.longitude,
            speed = p.speed,
            battery = p.battery.toInt(),
            heading = p.heading,
            status = p.status,
            timestamp = p.timestamp,
        )

        val carId = vehicleId.toLongOrNull() ?: run {
            log.warn("vehicleId '{}' 를 Long으로 변환할 수 없어 Kafka publish 생략", vehicleId)
            invalidVehicleIdCounter.increment()
            updateState(state)
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
        try {
            kafkaTemplate.send(vehicleDataTopic, carId.toString(), message).whenComplete { _, ex ->
                if (ex != null) {
                    kafkaPublishFailedCounter.increment()
                    logKafkaFailure(ex.message)
                } else {
                    kafkaPublishedCounter.increment()
                    kafkaFailCount.set(0)
                }
            }
        } catch (exception: Exception) {
            kafkaPublishFailedCounter.increment()
            logKafkaFailure(exception.message)
        }

        updateState(state)
    }

    private fun logKafkaFailure(message: String?) {
        val count = kafkaFailCount.incrementAndGet()
        val now = System.currentTimeMillis()
        val last = kafkaFailLoggedAt.get()
        // 최초 실패 또는 30초마다 한 번만 로그
        if (count == 1L || now - last > 30_000) {
            kafkaFailLoggedAt.set(now)
            log.warn("Kafka publish 실패 (누적 {}건): {}", count, message)
        }
    }

    private fun updateState(state: VehicleState) {
        try {
            stateStore.update(state)
        } catch (exception: Exception) {
            log.error("Vehicle state update 실패 [vehicleId={}]: {}", state.vehicleId, exception.message)
        }
    }
}
