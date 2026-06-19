package com.baro.dispatch.infrastructure.relocation

import com.baro.dispatch.application.port.out.RelocationDestination
import com.baro.dispatch.application.port.out.RelocationPort
import com.fasterxml.jackson.annotation.JsonProperty
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class RelocationServiceClient(
    @Value("\${relocation.service.url}") baseUrl: String,
) : RelocationPort {

    private val log = LoggerFactory.getLogger(javaClass)
    private val client = RestClient.builder()
        .baseUrl(baseUrl)
        .requestFactory(org.springframework.http.client.SimpleClientHttpRequestFactory().apply {
            setConnectTimeout(3_000)
            setReadTimeout(5_000)
        })
        .build()

    override fun assignRelocation(carId: Long, currentLat: Double, currentLon: Double): RelocationDestination {
        log.info("재배치 정거장 조회. carId={}, lat={}, lon={}", carId, currentLat, currentLon)
        val response = client.post()
            .uri("/relocation/assign")
            .body(mapOf("carId" to carId, "currentLat" to currentLat, "currentLon" to currentLon))
            .retrieve()
            .body(RelocationAssignResponse::class.java)
            ?: throw IllegalStateException("재배치 서비스 응답이 없습니다. carId=$carId")

        return RelocationDestination(
            targetStandId = response.targetStandId,
            targetLat = response.targetLat,
            targetLon = response.targetLon,
        )
    }

    private data class RelocationAssignResponse(
        val carId: Long,
        @JsonProperty("targetStandId") val targetStandId: String,
        @JsonProperty("targetLat") val targetLat: Double,
        @JsonProperty("targetLon") val targetLon: Double,
    )
}
