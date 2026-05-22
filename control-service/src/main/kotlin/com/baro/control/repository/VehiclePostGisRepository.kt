package com.baro.control.repository

import com.baro.control.dto.VehicleStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository

@Repository
class VehiclePostGisRepository(private val jdbc: JdbcTemplate) {

    fun updateLocation(vehicleId: String, lat: Double, lng: Double) {
        jdbc.update("""
            INSERT INTO vehicle_status (vehicle_id, location, latitude, longitude, updated_at)
            VALUES (?, ST_SetSRID(ST_MakePoint(?, ?), 4326), ?, ?, NOW())
            ON CONFLICT (vehicle_id) DO UPDATE SET
                location  = EXCLUDED.location,
                latitude  = EXCLUDED.latitude,
                longitude = EXCLUDED.longitude,
                updated_at = NOW()
        """, vehicleId, lng, lat, lat, lng)
    }

    fun updateTelemetryInfo(vehicleId: String, info: Map<String, String>) {
        jdbc.update("""
            INSERT INTO vehicle_status
                (vehicle_id, speed, heading, battery, autonomy_mode, status, trip_id, last_seen, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())
            ON CONFLICT (vehicle_id) DO UPDATE SET
                speed         = COALESCE(EXCLUDED.speed,         vehicle_status.speed),
                heading       = COALESCE(EXCLUDED.heading,       vehicle_status.heading),
                battery       = COALESCE(EXCLUDED.battery,       vehicle_status.battery),
                autonomy_mode = COALESCE(EXCLUDED.autonomy_mode, vehicle_status.autonomy_mode),
                status        = COALESCE(EXCLUDED.status,        vehicle_status.status),
                trip_id       = COALESCE(EXCLUDED.trip_id,       vehicle_status.trip_id),
                last_seen     = COALESCE(EXCLUDED.last_seen,     vehicle_status.last_seen),
                updated_at    = NOW()
        """,
            vehicleId,
            info["speed"]?.toIntOrNull(),
            info["heading"]?.toDoubleOrNull(),
            info["battery"]?.toDoubleOrNull(),
            info["autonomyMode"],
            info["status"],
            info["tripId"]?.takeIf { it.isNotEmpty() },
            info["lastSeen"],
        )
    }

    fun updateSnapshotInfo(vehicleId: String, info: Map<String, String>) {
        jdbc.update("""
            INSERT INTO vehicle_status
                (vehicle_id, battery, engine_oil, brake_oil, washer_fluid, ext_temp, last_seen, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, NOW())
            ON CONFLICT (vehicle_id) DO UPDATE SET
                battery      = COALESCE(EXCLUDED.battery,      vehicle_status.battery),
                engine_oil   = COALESCE(EXCLUDED.engine_oil,   vehicle_status.engine_oil),
                brake_oil    = COALESCE(EXCLUDED.brake_oil,    vehicle_status.brake_oil),
                washer_fluid = COALESCE(EXCLUDED.washer_fluid, vehicle_status.washer_fluid),
                ext_temp     = COALESCE(EXCLUDED.ext_temp,     vehicle_status.ext_temp),
                last_seen    = COALESCE(EXCLUDED.last_seen,    vehicle_status.last_seen),
                updated_at   = NOW()
        """,
            vehicleId,
            info["battery"]?.toDoubleOrNull(),
            info["engineOil"]?.toDoubleOrNull(),
            info["brakeOil"]?.toDoubleOrNull(),
            info["washerFluid"]?.toDoubleOrNull(),
            info["extTemp"]?.toDoubleOrNull(),
            info["lastSeen"],
        )
    }

    fun allVehicleIds(): Set<String> =
        jdbc.queryForList("SELECT vehicle_id FROM vehicle_status", String::class.java).toSet()

    fun getStatus(vehicleId: String): VehicleStatus {
        val results = jdbc.query("""
            SELECT vehicle_id, latitude, longitude, speed, heading, battery,
                   autonomy_mode, status, trip_id, last_seen
            FROM vehicle_status
            WHERE vehicle_id = ?
        """, rowMapper, vehicleId)
        return results.firstOrNull()
            ?: VehicleStatus(vehicleId, null, null, null, null, null, null, null, null, null)
    }

    private val rowMapper = RowMapper { rs, _ ->
        VehicleStatus(
            vehicleId   = rs.getString("vehicle_id"),
            latitude    = rs.getDouble("latitude").takeIf { !rs.wasNull() },
            longitude   = rs.getDouble("longitude").takeIf { !rs.wasNull() },
            speed       = rs.getInt("speed").takeIf { !rs.wasNull() },
            heading     = rs.getDouble("heading").takeIf { !rs.wasNull() },
            battery     = rs.getDouble("battery").takeIf { !rs.wasNull() },
            autonomyMode = rs.getString("autonomy_mode"),
            status      = rs.getString("status"),
            tripId      = rs.getString("trip_id"),
            lastSeen    = rs.getString("last_seen"),
        )
    }
}
