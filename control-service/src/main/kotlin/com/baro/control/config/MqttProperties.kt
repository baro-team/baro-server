package com.baro.control.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "mqtt")
data class MqttProperties(
    val mode: String = "local",
    val clientId: String = "control-service",
    val local: LocalProps = LocalProps(),
    val aws: AwsProps = AwsProps(),
) {
    data class LocalProps(
        val host: String = "localhost",
        val port: Int = 1883,
    )

    data class AwsProps(
        val endpoint: String = "",
        val certPath: String = "",
        val keyPath: String = "",
        val caPath: String = "",
    )
}
