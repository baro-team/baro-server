package com.baro.dispatch.infrastructure.redis

import com.baro.dispatch.application.port.out.DispatchableCarProjection
import com.baro.dispatch.application.port.out.DispatchableCarCandidate
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
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
private fun carNumberKey(carId: Long) = "dispatch:car:$carId:number"

@Component
class RedisDispatchableCarProjection(
    private val redisTemplate: RedisTemplate<String, String>,
    private val properties: DispatchRedisProperties,
    meterRegistry: MeterRegistry,
) : DispatchableCarProjection {

    private val log = LoggerFactory.getLogger(javaClass)
    private val candidateSearchCounter = Counter.builder("baro_dispatch_idle_geo_candidate_search_total").register(meterRegistry)
    private val candidateNotFoundCounter = Counter.builder("baro_dispatch_idle_geo_candidate_not_found_total").register(meterRegistry)
    private val staleCleanupCounter = Counter.builder("baro_dispatch_idle_geo_stale_cleanup_total").register(meterRegistry)

    init {
        Gauge.builder("baro_dispatch_idle_geo_count") { readIdleGeoCount() }
            .register(meterRegistry)
    }

    override fun saveIdleCarLocation(carId: Long, carNumber: String?, latitude: Double, longitude: Double) {
        log.info("Redis GEO에 배차 가능 차량을 저장합니다. carId={}, key={}", carId, properties.idleCarGeoKey)
        redisTemplate.opsForGeo().add(properties.idleCarGeoKey, Point(longitude, latitude), carId.toString())
        // TTL = stalenessThresholdSeconds: 이 시간 동안 업데이트 없으면 자동 만료
        redisTemplate.opsForValue().set(
            carMetaKey(carId),
            System.currentTimeMillis().toString(),
            Duration.ofSeconds(properties.stalenessThresholdSeconds),
        )
        if (!carNumber.isNullOrBlank()) {
            redisTemplate.opsForValue().set(
                carNumberKey(carId),
                carNumber,
                Duration.ofSeconds(properties.stalenessThresholdSeconds),
            )
        } else {
            redisTemplate.delete(carNumberKey(carId))
        }
    }

    override fun removeCar(carId: Long) {
        log.info("Redis GEO에서 배차 가능 차량을 제거합니다. carId={}, key={}", carId, properties.idleCarGeoKey)
        removeCarsFromProjection(listOf(carId))
    }

    override fun findNearestIdleCars(latitude: Double, longitude: Double): List<DispatchableCarCandidate> {
        val maxRadiusKm = searchRadiiKm().last()
        val candidates = findNearestIdleCars(latitude, longitude, maxRadiusKm)

        if (candidates.isEmpty()) {
            log.warn("반경 {}km 내 유효한(non-stale) 배차 가능 차량이 없습니다.", maxRadiusKm)
            candidateNotFoundCounter.increment()
        }
        return candidates
    }

    private fun findNearestIdleCars(latitude: Double, longitude: Double, radiusKm: Double): List<DispatchableCarCandidate> {
        val results = redisTemplate.opsForGeo().search(
            properties.idleCarGeoKey,
            GeoReference.fromCoordinate(longitude, latitude),
            Distance(radiusKm, Metrics.KILOMETERS),
            RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs()
                .includeDistance()
                .includeCoordinates()
                .sortAscending()
                .limit(properties.idleCarMaxCandidates.toLong()),
        ) ?: return emptyList()

        candidateSearchCounter.increment()

        val thresholdMs = properties.stalenessThresholdSeconds * 1_000L
        val now = System.currentTimeMillis()

        val carIds = results.map { it.content.name.toLong() }
        if (carIds.isEmpty()) return emptyList()

        val lastSeenValues = redisTemplate.opsForValue()
            .multiGet(carIds.map { carMetaKey(it) }) ?: emptyList()
        val carNumberValues = redisTemplate.opsForValue()
            .multiGet(carIds.map { carNumberKey(it) }) ?: emptyList()
        val staleCarIds = mutableListOf<Long>()
        val candidates = mutableListOf<DispatchableCarCandidate>()

        for ((index, result) in results.withIndex()) {
            val carId = carIds[index]
            val lastSeenMs = lastSeenValues.getOrNull(index)?.toLongOrNull()

            if (lastSeenMs == null || now - lastSeenMs > thresholdMs) {
                log.warn("stale 차량 제외: carId={}, lastSeen={}ms 전", carId, lastSeenMs?.let { now - it } ?: "없음")
                staleCarIds += carId
                continue
            }

            val point = result.content.point
                ?: throw IllegalStateException("Redis GEO 검색 결과에 좌표가 없습니다. carId=${result.content.name}")

            candidates += DispatchableCarCandidate(
                carId = carId,
                carNumber = carNumberValues.getOrNull(index),
                distanceKm = result.distance.value,
                latitude = point.y,
                longitude = point.x,
            )
        }

        removeStaleCars(staleCarIds)

        if (candidates.isEmpty()) {
            log.warn("반경 {}km 내 유효한(non-stale) 배차 가능 차량이 없습니다.", radiusKm)
        }
        return candidates
    }

    private fun searchRadiiKm(): List<Double> =
        properties.idleCarSearchRadiiKm
            .filter { it > 0.0 }
            .distinct()
            .sorted()
            .ifEmpty { listOf(properties.idleCarSearchRadiusKm) }

    private fun removeStaleCars(carIds: List<Long>) {
        if (carIds.isEmpty()) return

        try {
            log.info("stale 배차 가능 차량을 Redis GEO에서 정리합니다. carIds={}", carIds)
            staleCleanupCounter.increment()
            removeCarsFromProjection(carIds)
        } catch (exception: Exception) {
            log.error("stale 차량 정리 중 오류가 발생했습니다. carIds={}", carIds, exception)
        }
    }

    private fun removeCarsFromProjection(carIds: List<Long>) {
        if (carIds.isEmpty()) return

        redisTemplate.opsForGeo().remove(
            properties.idleCarGeoKey,
            *carIds.map { it.toString() }.toTypedArray(),
        )
        redisTemplate.delete(carIds.flatMap { listOf(carMetaKey(it), carNumberKey(it)) })
    }

    private fun readIdleGeoCount(): Double = try {
        (redisTemplate.execute { connection -> connection.zCard(properties.idleCarGeoKey.toByteArray()) } ?: 0L).toDouble()
    } catch (_: Exception) {
        Double.NaN
    }
}
