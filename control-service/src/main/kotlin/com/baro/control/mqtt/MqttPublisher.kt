package com.baro.control.mqtt

import com.baro.control.dto.CommandRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.integration.mqtt.support.MqttHeaders
import org.springframework.integration.support.MessageBuilder
import org.springframework.messaging.MessageChannel
import org.springframework.stereotype.Component

@Component
class MqttPublisher(
    @Qualifier("mqttOutboundChannel") private val outbound: MessageChannel,
    private val objectMapper: ObjectMapper,
) {
    fun sendCommand(vehicleId: String, command: CommandRequest) {
        val msg = MessageBuilder.withPayload(objectMapper.writeValueAsString(command))
            .setHeader(MqttHeaders.TOPIC, "vehicles/$vehicleId/commands")
            .setHeader(MqttHeaders.QOS, 1)
            .build()
        outbound.send(msg)
    }
}
