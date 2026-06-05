package com.baro.dispatch.interfaces.rest

import com.baro.common.web.response.BaseResponse
import com.baro.dispatch.application.service.ActiveDispatchResult
import com.baro.dispatch.application.service.ActiveDispatchService
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(DispatchApiPaths.DISPATCH)
class ActiveDispatchController(
    private val activeDispatchService: ActiveDispatchService,
) {
    @GetMapping("/vehicles/{carId}/active")
    fun getActiveDispatch(@PathVariable carId: Long): BaseResponse<ActiveDispatchResponse?> =
        BaseResponse.success(
            activeDispatchService.getActiveDispatch(carId)?.let { ActiveDispatchResponse.from(it) }
        )
}

data class ActiveDispatchResponse(
    @JsonProperty("dispatch_id") val dispatchId: Long,
    @JsonProperty("car_id") val carId: Long,
    val origin: LocationResponse,
    val destination: LocationResponse,
    @JsonProperty("estimated_pickup_time") val estimatedPickupTime: Int,
    @JsonProperty("estimated_ride_time") val estimatedRideTime: Int,
    val fare: Int,
) {
    companion object {
        fun from(result: ActiveDispatchResult) = ActiveDispatchResponse(
            dispatchId = result.dispatchId,
            carId = result.carId,
            origin = LocationResponse(result.origin.latitude, result.origin.longitude, result.origin.name),
            destination = LocationResponse(result.destination.latitude, result.destination.longitude, result.destination.name),
            estimatedPickupTime = result.estimatedPickupTime,
            estimatedRideTime = result.estimatedRideTime,
            fare = result.fare,
        )
    }
}

data class LocationResponse(
    val latitude: Double,
    val longitude: Double,
    val name: String? = null,
)
