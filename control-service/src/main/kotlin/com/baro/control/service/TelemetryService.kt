package com.baro.control.service

import com.baro.control.dto.SnapshotPayload
import com.baro.control.dto.TelemetryPayload
import com.baro.control.repository.VehiclePostGisRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class TelemetryService(
    private val repo: VehiclePostGisRepository,
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    @Value("\${kafka.topic.vehicle-data}") private val vehicleDataTopic: String,
) {
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

        val message = mapOf(
            "car_id"    to vehicleId.toLongOrNull(),
            "latitude"  to p.latitude,
            "longitude" to p.longitude,
            "speed"     to p.speed,
            "battery"   to p.battery.toInt(),
            "heading"   to p.heading,
            "timestamp" to p.timestamp,
        )
        kafkaTemplate.send(vehicleDataTopic, vehicleId, message)
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
