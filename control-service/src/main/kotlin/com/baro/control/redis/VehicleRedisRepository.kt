package com.baro.control.redis

import com.baro.control.dto.VehicleStatus
import org.springframework.data.geo.Point
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository

private const val GEO_KEY = "vehicle:locations"
private const val IDS_KEY = "vehicle:ids"

@Repository
class VehicleRedisRepository(private val redis: StringRedisTemplate) {

    fun updateLocation(vehicleId: String, lat: Double, lng: Double) {
        redis.opsForGeo().add(GEO_KEY, Point(lng, lat), vehicleId)
    }

    fun updateInfo(vehicleId: String, info: Map<String, String>) {
        redis.opsForHash<String, String>().putAll("car:$vehicleId:info", info)
        redis.opsForSet().add(IDS_KEY, vehicleId)
    }

    fun allVehicleIds(): Set<String> =
        redis.opsForSet().members(IDS_KEY) ?: emptySet()

    fun getStatus(vehicleId: String): VehicleStatus {
        val info = redis.opsForHash<String, String>().entries("car:$vehicleId:info")
        val pos = redis.opsForGeo().position(GEO_KEY, vehicleId)?.firstOrNull()
        return VehicleStatus(
            vehicleId = vehicleId,
            latitude = pos?.y,
            longitude = pos?.x,
            speed = info["speed"]?.toIntOrNull(),
            heading = info["heading"]?.toDoubleOrNull(),
            battery = info["battery"]?.toDoubleOrNull(),
            autonomyMode = info["autonomyMode"],
            status = info["status"],
            tripId = info["tripId"]?.takeIf { it.isNotEmpty() },
            lastSeen = info["lastSeen"],
        )
    }
}
