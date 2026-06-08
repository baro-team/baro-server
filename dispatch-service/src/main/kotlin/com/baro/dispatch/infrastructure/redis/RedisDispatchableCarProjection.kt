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
import java.time.Duration

private fun carMetaKey(carId: Long) = "dispatch:car:$carId"

@Component
class RedisDispatchableCarProjection(
    private val redisTemplate: RedisTemplate<String, String>,
    private val properties: DispatchRedisProperties,
) : DispatchableCarProjection {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun saveIdleCarLocation(carId: Long, latitude: Double, longitude: Double) {
        log.info("Redis GEO에 배차 가능 차량을 저장합니다. carId={}, key={}", carId, properties.idleCarGeoKey)
        redisTemplate.opsForGeo().add(properties.idleCarGeoKey, Point(longitude, latitude), carId.toString())
        // TTL = stalenessThresholdSeconds: 이 시간 동안 업데이트 없으면 자동 만료
        redisTemplate.opsForValue().set(
            carMetaKey(carId),
            System.currentTimeMillis().toString(),
            Duration.ofSeconds(properties.stalenessThresholdSeconds),
        )
    }

    override fun removeCar(carId: Long) {
        log.info("Redis GEO에서 배차 가능 차량을 제거합니다. carId={}, key={}", carId, properties.idleCarGeoKey)
        redisTemplate.opsForGeo().remove(properties.idleCarGeoKey, carId.toString())
        redisTemplate.delete(carMetaKey(carId))
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
                .limit(properties.idleCarMaxCandidates.toLong()),
        ) ?: return null

        val thresholdMs = properties.stalenessThresholdSeconds * 1_000L
        val now = System.currentTimeMillis()

        for (result in results) {
            val carId = result.content.name.toLong()
            val lastSeenMs = redisTemplate.opsForValue().get(carMetaKey(carId))?.toLongOrNull()

            if (lastSeenMs == null || now - lastSeenMs > thresholdMs) {
                log.warn("stale 차량 제외: carId={}, lastSeen={}ms 전", carId, lastSeenMs?.let { now - it } ?: "없음")
                continue
            }

            val point = result.content.point
                ?: throw IllegalStateException("Redis GEO 검색 결과에 좌표가 없습니다. carId=${result.content.name}")

            return DispatchableCarCandidate(
                carId = carId,
                distanceKm = result.distance.value,
                latitude = point.y,
                longitude = point.x,
            )
        }

        log.warn("반경 {}km 내 유효한(non-stale) 배차 가능 차량이 없습니다.", properties.idleCarSearchRadiusKm)
        return null
    }
}
