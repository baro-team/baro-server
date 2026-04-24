package com.baro.control.service

import com.baro.control.dto.SnapshotPayload
import com.baro.control.dto.TelemetryPayload
import com.baro.control.redis.RedisPublisher
import com.baro.control.redis.VehicleRedisRepository
import org.springframework.stereotype.Service

@Service
class TelemetryService(
    private val repo: VehicleRedisRepository,
    private val publisher: RedisPublisher,
) {
    fun handleTelemetry(vehicleId: String, p: TelemetryPayload) {
        repo.updateLocation(vehicleId, p.latitude, p.longitude)
        repo.updateInfo(
            vehicleId, mapOf(
                "speed" to p.speed.toString(),
                "heading" to p.heading.toString(),
                "battery" to p.battery.toString(),
                "autonomyMode" to p.autonomyMode,
                "status" to p.status,
                "tripId" to (p.tripId ?: ""),
                "lastSeen" to p.timestamp,
            )
        )
        publisher.publish("vehicle-location:$vehicleId", p)
    }

    fun handleSnapshot(vehicleId: String, p: SnapshotPayload) {
        repo.updateInfo(
            vehicleId, mapOf(
                "battery" to p.battery.toString(),
                "engineOil" to p.engineOil.toString(),
                "brakeOil" to p.brakeOil.toString(),
                "washerFluid" to p.washerFluid.toString(),
                "extTemp" to p.extTemp.toString(),
                "lastSeen" to p.timestamp,
            )
        )
    }
}
