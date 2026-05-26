package com.baro.control.service

import com.baro.control.dto.SnapshotPayload
import com.baro.control.dto.TelemetryPayload
import com.baro.control.repository.VehiclePostGisRepository
import org.springframework.stereotype.Service

@Service
class TelemetryService(
    private val repo: VehiclePostGisRepository,
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
