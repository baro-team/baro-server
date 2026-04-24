package com.baro.control.controller

import com.baro.control.dto.CommandRequest
import com.baro.control.dto.VehicleStatus
import com.baro.control.mqtt.MqttPublisher
import com.baro.control.redis.VehicleRedisRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/vehicles")
class VehicleController(
    private val repo: VehicleRedisRepository,
    private val mqttPublisher: MqttPublisher,
) {
    @GetMapping
    fun listVehicles(): List<VehicleStatus> =
        repo.allVehicleIds().map { repo.getStatus(it) }

    @GetMapping("/{id}")
    fun getVehicle(@PathVariable id: String): ResponseEntity<VehicleStatus> {
        val status = repo.getStatus(id)
        return if (status.lastSeen != null) ResponseEntity.ok(status)
        else ResponseEntity.notFound().build()
    }

    @PostMapping("/{id}/command")
    fun sendCommand(
        @PathVariable id: String,
        @RequestBody command: CommandRequest,
    ): ResponseEntity<Map<String, String>> {
        mqttPublisher.sendCommand(id, command)
        return ResponseEntity.ok(mapOf("status" to "sent", "vehicleId" to id, "type" to command.type))
    }
}
