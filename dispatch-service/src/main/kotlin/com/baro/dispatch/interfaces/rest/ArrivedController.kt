package com.baro.dispatch.interfaces.rest

import com.baro.dispatch.application.service.ArrivedService
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(DispatchApiPaths.DISPATCH)
class ArrivedController(
    private val arrivedService: ArrivedService,
) {
    @PostMapping("/arrived")
    fun arrived(@RequestBody request: ArrivedRequest) {
        arrivedService.handleArrived(request.vehicleId, request.tripId, request.phase)
    }
}

data class ArrivedRequest(
    @JsonProperty("vehicleId") val vehicleId: String,
    @JsonProperty("tripId") val tripId: String,
    @JsonProperty("phase") val phase: String = "to_pickup",
)
