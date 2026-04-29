package com.baro.dispatch.application.service

import com.baro.dispatch.application.port.out.DirectionsPort
import com.baro.dispatch.domain.model.DispatchRequest
import com.baro.dispatch.domain.model.GeoPoint
import com.baro.dispatch.domain.repository.DispatchRequestRepository
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.OffsetDateTime
import kotlin.math.ceil
import kotlin.math.round

@Service
class PreDispatchService(
    private val directionsPort: DirectionsPort,
    private val dispatchRequestRepository: DispatchRequestRepository,
    private val clock: Clock,
) {
    fun estimate(command: PreDispatchCommand): PreDispatchResult {
        val routeEstimate = directionsPort.findRoute(command.origin, command.destination)
        val requestId = dispatchRequestRepository.save(
            DispatchRequest.pending(
                userId = command.userId,
                origin = command.origin,
                destination = command.destination,
                now = OffsetDateTime.now(clock),
            ),
        )

        return PreDispatchResult(
            requestId = requestId,
            fare = routeEstimate.fare,
            routePath = routeEstimate.routePath,
            estimatedTime = ceil(routeEstimate.durationSeconds / SECONDS_PER_MINUTE).toInt(),
            distanceKm = round(routeEstimate.distanceMeters / METERS_PER_KILOMETER * 10) / 10,
        )
    }

    private companion object {
        const val SECONDS_PER_MINUTE = 60.0
        const val METERS_PER_KILOMETER = 1000.0
    }
}

data class PreDispatchCommand(
    val userId: Long,
    val origin: GeoPoint,
    val destination: GeoPoint,
) {
    init {
        require(userId > 0) { "사용자 ID는 양수여야 합니다." }
    }
}

data class PreDispatchResult(
    val requestId: Long,
    val fare: Int,
    val routePath: List<GeoPoint>,
    val estimatedTime: Int,
    val distanceKm: Double,
)
