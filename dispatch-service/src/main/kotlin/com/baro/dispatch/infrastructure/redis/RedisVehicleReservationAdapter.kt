package com.baro.dispatch.infrastructure.redis

import com.baro.dispatch.application.port.out.VehicleReservationPort
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisVehicleReservationAdapter(
    private val redisTemplate: RedisTemplate<String, String>,
    private val properties: DispatchRedisProperties,
) : VehicleReservationPort {
    override fun reserve(carId: Long, ownerId: String): Boolean =
        redisTemplate.opsForValue()
            .setIfAbsent(reservationKey(carId), ownerId, Duration.ofSeconds(properties.reservationTtlSeconds)) == true

    override fun release(carId: Long, ownerId: String) {
        redisTemplate.execute(RELEASE_SCRIPT, listOf(reservationKey(carId)), ownerId)
    }

    private fun reservationKey(carId: Long): String = "${properties.reservationKeyPrefix}$carId"

    private companion object {
        val RELEASE_SCRIPT = DefaultRedisScript(
            """
            if redis.call('GET', KEYS[1]) == ARGV[1] then
              return redis.call('DEL', KEYS[1])
            end
            return 0
            """.trimIndent(),
            Long::class.java,
        )
    }
}
