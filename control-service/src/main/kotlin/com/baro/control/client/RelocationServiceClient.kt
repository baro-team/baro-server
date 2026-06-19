package com.baro.control.client

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.net.http.HttpClient
import java.time.Duration

@Component
class RelocationServiceClient(
    restClientBuilder: RestClient.Builder,
    @Value("\${relocation.service.url}") baseUrl: String,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val client = restClientBuilder.clone()
        .baseUrl(baseUrl)
        .requestFactory(
            JdkClientHttpRequestFactory(
                HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(3_000))
                    .build()
            ).apply { setReadTimeout(Duration.ofMillis(5_000)) }
        )
        .build()

    fun notifyVehicleCompleted(carId: Long, lat: Double, lon: Double) {
        log.info("재배치 서비스에 운행 완료 통보. carId={}", carId)
        client.post()
            .uri("/relocation/internal/vehicle-completed")
            .body(mapOf("carId" to carId, "lat" to lat, "lon" to lon))
            .retrieve()
            .toBodilessEntity()
    }
}
