package com.baro.dispatch.application.service

import com.baro.dispatch.domain.model.GeoPoint
import com.baro.dispatch.domain.repository.DispatchRepository
import com.baro.dispatch.domain.repository.DispatchRequestRepository
import org.springframework.stereotype.Service

@Service
class ActiveDispatchService(
    private val dispatchRepository: DispatchRepository,
    private val dispatchRequestRepository: DispatchRequestRepository,
) {
    fun getActiveDispatch(carId: Long): ActiveDispatchResult? {
        val dispatch = dispatchRepository.findActiveByCarId(carId) ?: return null
        val dispatchId = requireNotNull(dispatch.id) { "Active dispatch for car $carId has no ID" }
        val request = dispatchRequestRepository.findById(dispatch.requestId)
            ?: throw IllegalStateException("Dispatch request ${dispatch.requestId} not found for dispatch $dispatchId")

        return ActiveDispatchResult(
            dispatchId = dispatchId,
            carId = carId,
            origin = request.origin,
            destination = request.destination,
            estimatedPickupTime = dispatch.estimatedPickupTime,
            estimatedRideTime = dispatch.estimatedRideTime,
            pickupRoutePath = dispatch.pickupRoutePath,
            dropoffRoutePath = dispatch.dropoffRoutePath,
            fare = dispatch.fare,
        )
    }
}

data class ActiveDispatchResult(
    val dispatchId: Long,
    val carId: Long,
    val origin: GeoPoint,
    val destination: GeoPoint,
    val estimatedPickupTime: Int,
    val estimatedRideTime: Int,
    val pickupRoutePath: List<GeoPoint>,
    val dropoffRoutePath: List<GeoPoint>,
    val fare: Int,
)
