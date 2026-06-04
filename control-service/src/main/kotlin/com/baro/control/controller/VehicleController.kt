package com.baro.control.controller

import com.baro.control.dto.CommandRequest
import com.baro.control.mqtt.MqttPublisher
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/vehicles")
class VehicleController(
    private val mqttPublisher: MqttPublisher,
) {
    @PostMapping("/{id}/command")
    fun sendCommand(
        @PathVariable id: String,
        @RequestBody command: CommandRequest,
    ): ResponseEntity<Map<String, String>> {
        mqttPublisher.sendCommand(id, command)
        return ResponseEntity.ok(mapOf("status" to "sent", "vehicleId" to id, "type" to command.type))
    }
}
