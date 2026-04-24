package com.baro.control.redis

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class RedisPublisher(
    private val redis: StringRedisTemplate,
    private val objectMapper: ObjectMapper,
) {
    fun publish(channel: String, payload: Any) {
        redis.convertAndSend(channel, objectMapper.writeValueAsString(payload))
    }
}
