package com.baro.control.service

import com.baro.control.client.DispatchServiceClient
import com.baro.control.dto.EventPayload
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class EventService(private val dispatchClient: DispatchServiceClient) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun handleEvent(vehicleId: String, p: EventPayload) {
        when (p.eventType) {
            "ARRIVED" -> {
                log.info("[{}] ARRIVED trip={}", vehicleId, p.tripId)
                p.tripId?.let { dispatchClient.notifyArrived(vehicleId, it) }
            }
            "WARNING" -> {
                log.warn("[{}] WARNING code={} detail={}", vehicleId, p.code, p.detail)
            }
            else -> log.info("[{}] event={}", vehicleId, p.eventType)
        }
    }
}
