package com.baro.dispatch.infrastructure.redis

import com.baro.dispatch.application.port.out.DispatchableCarProjection
import org.slf4j.LoggerFactory
import org.springframework.data.geo.Point
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class RedisDispatchableCarProjection(
    private val redisTemplate: RedisTemplate<String, String>,
    private val properties: DispatchRedisProperties,
) : DispatchableCarProjection {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun saveIdleCarLocation(carId: Long, latitude: Double, longitude: Double) {
        log.info("Redis GEO에 배차 가능 차량을 저장합니다. carId={}, key={}", carId, properties.idleCarGeoKey)
        redisTemplate.opsForGeo().add(properties.idleCarGeoKey, Point(longitude, latitude), carId.toString())
    }

    override fun removeCar(carId: Long) {
        log.info("Redis GEO에서 배차 가능 차량을 제거합니다. carId={}, key={}", carId, properties.idleCarGeoKey)
        redisTemplate.opsForGeo().remove(properties.idleCarGeoKey, carId.toString())
    }
}
