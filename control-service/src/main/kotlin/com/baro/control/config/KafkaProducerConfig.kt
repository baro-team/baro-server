package com.baro.control.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
class KafkaProducerConfig(private val objectMapper: ObjectMapper) {

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Bean
    fun producerFactory(): ProducerFactory<String, Any> {
        val valueSerializer = JsonSerializer<Any>(objectMapper).apply { setAddTypeInfo(false) }
        return DefaultKafkaProducerFactory(
            mapOf(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
                // Kafka 미연결 시 빠른 실패 → MQTT 수신 스레드 블로킹 방지 (0은 metadata 조회도 차단)
                ProducerConfig.MAX_BLOCK_MS_CONFIG to 500,
                ProducerConfig.RETRIES_CONFIG to 0,
            ),
            StringSerializer(),
            valueSerializer,
        )
    }

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, Any> = KafkaTemplate(producerFactory())
}
