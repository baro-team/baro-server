package com.baro.dispatch.infrastructure.control

import com.baro.dispatch.application.port.out.ControlPort
import com.baro.dispatch.domain.model.GeoPoint
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class ControlServiceClient(
    @Value("\${control.service.url}") baseUrl: String,
) : ControlPort {

    private val log = LoggerFactory.getLogger(javaClass)
    private val client = RestClient.builder()
        .baseUrl(baseUrl)
        .requestFactory(org.springframework.http.client.SimpleClientHttpRequestFactory().apply {
            setConnectTimeout(3_000)
            setReadTimeout(5_000)
        })
        .build()

    override fun sendDispatchCommand(
        carId: Long,
        tripId: String,
        route: List<GeoPoint>,
        distanceMeters: Int,
        durationSeconds: Int,
        phase: String,
    ) {
        log.info("차량에 배차 명령을 전송합니다. carId={}, tripId={}, phase={}, points={}", carId, tripId, phase, route.size)
        val body = mapOf(
            "type" to "DISPATCH",
            "trip_id" to tripId,
            "route" to route.map { mapOf("lat" to it.latitude, "lng" to it.longitude) },
            "phase" to phase,
            "distance_m" to distanceMeters,
            "duration_s" to durationSeconds,
        )
        try {
            client.post()
                .uri("/control/vehicles/$carId/command")
                .body(body)
                .retrieve()
                .toBodilessEntity()
        } catch (e: Exception) {
            log.error("배차 명령 전송 실패: carId={}, tripId={}, err={}", carId, tripId, e.message)
            throw e
        }
    }

    override fun sendCancelDispatchCommand(carId: Long, tripId: String) {
        log.info("차량에 배차 취소 명령을 전송합니다. carId={}, tripId={}", carId, tripId)
        val body = mapOf(
            "type" to "CANCEL_DISPATCH",
            "trip_id" to tripId,
        )
        try {
            client.post()
                .uri("/control/vehicles/$carId/command")
                .body(body)
                .retrieve()
                .toBodilessEntity()
        } catch (e: Exception) {
            log.error("배차 취소 명령 전송 실패: carId={}, tripId={}, err={}", carId, tripId, e.message)
            throw e
        }
    }
}
