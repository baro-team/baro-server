package com.baro.control.mqtt

import com.baro.control.dto.AckPayload
import com.baro.control.dto.BufferedPayload
import com.baro.control.dto.EventPayload
import com.baro.control.dto.SnapshotPayload
import com.baro.control.dto.TelemetryPayload
import com.baro.control.service.EventService
import com.baro.control.service.TelemetryService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.integration.mqtt.support.MqttHeaders
import org.springframework.messaging.Message
import org.springframework.stereotype.Component

@Component
class MqttSubscriber(
    private val telemetryService: TelemetryService,
    private val eventService: EventService,
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @ServiceActivator(inputChannel = "mqttInputChannel")
    fun handle(message: Message<*>) {
        val topic = message.headers[MqttHeaders.RECEIVED_TOPIC] as? String ?: return
        val raw = when (val p = message.payload) {
            is String -> p
            is ByteArray -> String(p, Charsets.UTF_8)
            else -> return
        }

        // topic: vehicles/{vehicleId}/{type}[/subtype]
        val parts = topic.split("/")
        if (parts.size < 3 || parts[0] != "vehicles") return
        val vehicleId = parts[1]

        try {
            when {
                parts[2] == "telemetry" && parts.size == 3 -> {
                    telemetryService.handleTelemetry(vehicleId, objectMapper.readValue<TelemetryPayload>(raw))
                }
                parts[2] == "telemetry" && parts.getOrNull(3) == "buffered" -> {
                    val p = objectMapper.readValue<BufferedPayload>(raw)
                    p.buffered.forEach { telemetryService.handleTelemetry(vehicleId, it) }
                }
                parts[2] == "events" -> {
                    eventService.handleEvent(vehicleId, objectMapper.readValue<EventPayload>(raw))
                }
                parts[2] == "snapshot" -> {
                    telemetryService.handleSnapshot(vehicleId, objectMapper.readValue<SnapshotPayload>(raw))
                }
                parts[2] == "ack" -> {
                    val ack = objectMapper.readValue<AckPayload>(raw)
                    log.info("[{}] ACK: {} trip={}", vehicleId, ack.commandType, ack.tripId)
                }
            }
        } catch (e: Exception) {
            log.error("[{}] Failed to handle topic {}: {} - {}", vehicleId, topic, e.javaClass.simpleName, e.message, e)
        }
    }
}
