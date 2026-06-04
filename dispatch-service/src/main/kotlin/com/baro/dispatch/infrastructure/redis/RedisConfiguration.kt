package com.baro.dispatch.infrastructure.redis

import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@Configuration
@EnableConfigurationProperties(DispatchRedisProperties::class)
class RedisConfiguration {

    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, String> = StringRedisTemplate(connectionFactory)
}
