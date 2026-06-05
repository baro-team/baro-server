package com.baro.control.controller

import com.baro.control.dto.CommandRequest
import com.baro.control.dto.VehicleState
import com.baro.control.mqtt.MqttPublisher
import com.baro.control.service.VehicleStateStore
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/control/vehicles")
class VehicleController(
    private val mqttPublisher: MqttPublisher,
    private val stateStore: VehicleStateStore,
) {
    @GetMapping
    fun listVehicles(): List<VehicleState> = stateStore.findAll()

    @GetMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamVehicles(): SseEmitter = stateStore.subscribe()

    @PostMapping("/{id}/command")
    fun sendCommand(
        @PathVariable id: String,
        @RequestBody command: CommandRequest,
    ): ResponseEntity<Map<String, String>> {
        mqttPublisher.sendCommand(id, command)
        return ResponseEntity.ok(mapOf("status" to "sent", "vehicleId" to id, "type" to command.type))
    }
}
