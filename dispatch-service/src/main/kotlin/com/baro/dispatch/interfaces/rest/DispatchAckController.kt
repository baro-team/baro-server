package com.baro.dispatch.interfaces.rest

import com.baro.dispatch.application.service.PendingDispatchStore
import com.fasterxml.jackson.annotation.JsonProperty
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(DispatchApiPaths.DISPATCH)
class DispatchAckController(
    private val pendingStore: PendingDispatchStore,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @PostMapping("/command-ack")
    fun ack(@RequestBody request: DispatchAckRequest) {
        val dispatchId = request.tripId.toLongOrNull() ?: run {
            log.warn("유효하지 않은 tripId: {}", request.tripId)
            return
        }
        pendingStore.acknowledge(dispatchId)
        log.info("DISPATCH ACK 수신: vehicleId={}, dispatchId={}", request.vehicleId, dispatchId)
    }
}

data class DispatchAckRequest(
    @JsonProperty("vehicleId") val vehicleId: String,
    @JsonProperty("tripId") val tripId: String,
)
