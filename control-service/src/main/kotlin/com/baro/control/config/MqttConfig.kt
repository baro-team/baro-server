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

    // ECS Fargate: HOSTNAME = task ID 일부 → 다중 Task 배포 시 Client ID 충돌 방지
    private val instanceId: String =
        System.getenv("HOSTNAME")?.takeLast(8) ?: java.util.UUID.randomUUID().toString().takeLast(8)

    @Bean
    fun mqttClientFactory(): MqttPahoClientFactory {
        val options = MqttConnectOptions().apply {
            isCleanSession = false   // 재연결 시 IoT Core가 구독 세션 유지
            connectionTimeout = 30
            keepAliveInterval = 30
            isAutomaticReconnect = false  // Spring Integration 자체 재연결 사용 (Paho와 충돌 방지)
            when (props.mode) {
                // 인터넷 경유: 로컬 PC → 퍼블릭 IoT Core endpoint (포트 8883)
                "aws",
                // VPC PrivateLink 경유: ECS/EKS → 내부망 endpoint (포트·인증 방식 동일, endpoint URL만 상이)
                "aws-vpc" -> {
                    serverURIs = arrayOf("ssl://${props.aws.endpoint}:${props.aws.port}")
                    socketFactory = SslUtil.createSocketFactory(
                        props.aws.certPath,
                        props.aws.keyPath,
                        props.aws.caPath,
                    )
                }
                // local: 로컬 Mosquitto
                else -> serverURIs = arrayOf("tcp://${props.local.host}:${props.local.port}")
            }
        }
        return DefaultMqttPahoClientFactory().apply { connectionOptions = options }
    }

    @Bean
    fun mqttInputChannel(): MessageChannel = DirectChannel()

    @Bean
    fun mqttInboundAdapter(factory: MqttPahoClientFactory): MqttPahoMessageDrivenChannelAdapter {
        val adapter = MqttPahoMessageDrivenChannelAdapter(
            "${props.clientId}-sub",  // 영구 세션을 위해 고정 clientId 사용
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
        MqttPahoMessageHandler("${props.clientId}-pub-$instanceId", factory).apply {
            setAsync(true)
            setDefaultQos(1)
        }
}
