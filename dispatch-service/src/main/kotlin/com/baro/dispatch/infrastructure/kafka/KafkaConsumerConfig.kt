package com.baro.dispatch.infrastructure.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer

@Configuration
class KafkaConsumerConfig(private val objectMapper: ObjectMapper) {

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Value("\${spring.kafka.consumer.group-id}")
    private lateinit var groupId: String

    @Value("\${kafka.dispatch.concurrency:4}")
    private var concurrency: Int = 4

    @Bean
    fun carStateConsumerFactory(): ConsumerFactory<String, CarStateMessage> {
        val valueDeserializer = JsonDeserializer(CarStateMessage::class.java, objectMapper).apply {
            setRemoveTypeHeaders(true)
            addTrustedPackages("com.baro.dispatch.infrastructure.kafka")
            setUseTypeMapperForKey(false)
        }
        return DefaultKafkaConsumerFactory(
            mapOf(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG to groupId,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
            ),
            StringDeserializer(),
            valueDeserializer,
        )
    }

    @Bean
    fun carStateKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, CarStateMessage> =
        ConcurrentKafkaListenerContainerFactory<String, CarStateMessage>().apply {
            consumerFactory = carStateConsumerFactory()
            setConcurrency(concurrency)
        }
}
