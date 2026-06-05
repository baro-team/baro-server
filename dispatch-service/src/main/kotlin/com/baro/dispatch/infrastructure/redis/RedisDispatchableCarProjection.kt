package com.baro.dispatch.infrastructure.redis

import com.baro.dispatch.application.port.out.DispatchableCarProjection
import com.baro.dispatch.application.port.out.DispatchableCarCandidate
import org.springframework.data.geo.Distance
import org.springframework.data.geo.Metrics
import org.slf4j.LoggerFactory
import org.springframework.data.geo.Point
import org.springframework.data.redis.connection.RedisGeoCommands
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.domain.geo.GeoReference
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

    override fun findNearestIdleCar(latitude: Double, longitude: Double): DispatchableCarCandidate? {
        val results = redisTemplate.opsForGeo().search(
            properties.idleCarGeoKey,
            GeoReference.fromCoordinate(longitude, latitude),
            Distance(properties.idleCarSearchRadiusKm, Metrics.KILOMETERS),
            RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs()
                .includeDistance()
                .includeCoordinates()
                .sortAscending()
                .limit(1),
        )

        return results?.firstOrNull()?.let { result ->
            DispatchableCarCandidate(
                carId = result.content.name.toLong(),
                distanceKm = result.distance.value,
                latitude = result.content.point.y,
                longitude = result.content.point.x,
            )
        }
    }
}
