package com.baro.control.config

import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory
import org.springframework.integration.mqtt.core.MqttPahoClientFactory
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter
import org.springframework.messaging.MessageChannel

@Configuration
@EnableConfigurationProperties(MqttProperties::class)
class MqttConfig(private val props: MqttProperties) {

    @Bean
    fun mqttClientFactory(): MqttPahoClientFactory {
        val options = MqttConnectOptions().apply {
            isCleanSession = true
            connectionTimeout = 30
            keepAliveInterval = 30
            isAutomaticReconnect = true
            if (props.mode == "aws") {
                serverURIs = arrayOf("ssl://${props.aws.endpoint}:8883")
                socketFactory = SslUtil.createSocketFactory(
                    props.aws.certPath,
                    props.aws.keyPath,
                    props.aws.caPath,
                )
            } else {
                serverURIs = arrayOf("tcp://${props.local.host}:${props.local.port}")
            }
        }
        return DefaultMqttPahoClientFactory().apply { connectionOptions = options }
    }

    @Bean
    fun mqttInputChannel(): MessageChannel = DirectChannel()

    @Bean
    fun mqttInboundAdapter(factory: MqttPahoClientFactory): MqttPahoMessageDrivenChannelAdapter {
        val adapter = MqttPahoMessageDrivenChannelAdapter(
            "${props.clientId}-sub",
            factory,
            "vehicles/+/telemetry",
            "vehicles/+/telemetry/buffered",
            "vehicles/+/events",
            "vehicles/+/snapshot",
            "vehicles/+/ack",
        )
        adapter.setCompletionTimeout(5_000)
        adapter.setConverter(DefaultPahoMessageConverter())
        adapter.setQos(1)
        adapter.outputChannel = mqttInputChannel()
        return adapter
    }

    @Bean
    fun mqttOutboundChannel(): MessageChannel = DirectChannel()

    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    fun mqttOutboundHandler(factory: MqttPahoClientFactory): MqttPahoMessageHandler =
        MqttPahoMessageHandler("${props.clientId}-pub", factory).apply {
            setAsync(true)
            setDefaultQos(1)
        }
}
