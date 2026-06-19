package com.baro.relocation.client

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.net.http.HttpClient
import java.time.Duration

@Component
class ControlServiceClient(
    @Value("\${control.service.url}") baseUrl: String,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val client = RestClient.builder()
        .baseUrl(baseUrl)
        .requestFactory(
            JdkClientHttpRequestFactory(
                HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(3_000))
                    .build()
            ).apply { setReadTimeout(Duration.ofMillis(5_000)) }
        )
        .build()

    fun sendRelocateCommand(
        carId: Long,
        route: List<Map<String, Double>>,
        distanceMeters: Int,
        durationSeconds: Int,
    ) {
        log.info("차량에 재배치 명령 전송. carId={}, points={}", carId, route.size)
        val body = mapOf(
            "type" to "RELOCATE",
            "route" to route,
            "distance_m" to distanceMeters,
            "duration_s" to durationSeconds,
        )
        client.post()
            .uri("/control/vehicles/$carId/command")
            .body(body)
            .retrieve()
            .toBodilessEntity()
    }
}
