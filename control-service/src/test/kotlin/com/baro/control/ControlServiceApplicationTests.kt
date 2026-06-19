package com.baro.control

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.integration.mqtt.core.MqttPahoClientFactory
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler

@SpringBootTest
class ControlServiceApplicationTests {

    @MockBean
    lateinit var mqttClientFactory: MqttPahoClientFactory

    @MockBean
    lateinit var mqttInboundAdapter: MqttPahoMessageDrivenChannelAdapter

    @MockBean
    lateinit var mqttOutboundHandler: MqttPahoMessageHandler

    @Test
    fun contextLoads() {
    }
}
