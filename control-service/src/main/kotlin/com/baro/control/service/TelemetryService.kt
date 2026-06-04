package com.baro.control.service

import com.baro.control.dto.SnapshotPayload
import com.baro.control.dto.TelemetryPayload
import com.baro.control.repository.VehiclePostGisRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class TelemetryService(
    private val repo: VehiclePostGisRepository,
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    @Value("\${kafka.topic.vehicle-data}") private val vehicleDataTopic: String,
) {
    companion object { private val log = LoggerFactory.getLogger(TelemetryService::class.java) }

    fun handleTelemetry(vehicleId: String, p: TelemetryPayload) {
        repo.updateLocation(vehicleId, p.latitude, p.longitude)
        repo.updateTelemetryInfo(
            vehicleId, mapOf(
                "speed"        to p.speed.toString(),
                "heading"      to p.heading.toString(),
                "battery"      to p.battery.toString(),
                "autonomyMode" to p.autonomyMode,
                "status"       to p.status,
                "tripId"       to (p.tripId ?: ""),
                "lastSeen"     to p.timestamp,
            )
        )

        val carId = vehicleId.toLongOrNull() ?: run {
            log.warn("vehicleId '{}' 를 Long으로 변환할 수 없어 Kafka publish 생략", vehicleId)
            return
        }

        val message = mapOf(
            "car_id"    to carId,
            "latitude"  to p.latitude,
            "longitude" to p.longitude,
            "speed"     to p.speed,
            "battery"   to p.battery.toInt(),
            "heading"   to p.heading,
            "status"    to p.status,
            "timestamp" to p.timestamp,
        )
        kafkaTemplate.send(vehicleDataTopic, carId.toString(), message).whenComplete { _, ex ->
            if (ex != null) log.error("Kafka publish 실패 [carId={}]: {}", carId, ex.message)
        }
    }

    fun handleSnapshot(vehicleId: String, p: SnapshotPayload) {
        repo.updateSnapshotInfo(
            vehicleId, mapOf(
                "battery"      to p.battery.toString(),
                "engineOil"    to p.engineOil.toString(),
                "brakeOil"     to p.brakeOil.toString(),
                "washerFluid"  to p.washerFluid.toString(),
                "extTemp"      to p.extTemp.toString(),
                "lastSeen"     to p.timestamp,
            )
        )
    }
}
