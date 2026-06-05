package com.baro.control.client

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class DispatchServiceClient(
    @Value("\${dispatch.service.url}") baseUrl: String,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val client = RestClient.builder().baseUrl(baseUrl).build()

    fun notifyArrived(vehicleId: String, tripId: String, phase: String) {
        try {
            client.post()
                .uri("/dispatch/arrived")
                .body(mapOf("vehicleId" to vehicleId, "tripId" to tripId, "phase" to phase))
                .retrieve()
                .toBodilessEntity()
        } catch (e: Exception) {
            log.error("Failed to notify dispatch of arrival: vehicleId={} tripId={} phase={} err={}", vehicleId, tripId, phase, e.message)
        }
    }
}
