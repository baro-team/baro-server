package com.baro.dispatch.application.service

import com.baro.dispatch.application.port.out.ControlPort
import com.baro.dispatch.application.port.out.DirectionsPort
import com.baro.dispatch.application.port.out.DispatchableCarProjection
import com.baro.dispatch.application.port.out.VehicleReservationPort
import com.baro.dispatch.domain.model.GeoPoint
import com.baro.dispatch.domain.repository.DispatchRepository
import com.baro.dispatch.domain.repository.DispatchRequestRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import kotlin.math.ceil

@Service
class DispatchRetryService(
    private val pendingStore: PendingDispatchStore,
    private val dispatchRepository: DispatchRepository,
    private val dispatchRequestRepository: DispatchRequestRepository,
    private val dispatchableCarProjection: DispatchableCarProjection,
    private val vehicleReservationPort: VehicleReservationPort,
    private val directionsPort: DirectionsPort,
    private val controlPort: ControlPort,
    @Value("\${dispatch.ack-timeout-seconds:10}") private val ackTimeoutSeconds: Long,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 5_000)
    fun retryTimedOut() {
        val timedOut = pendingStore.pollTimedOut(ackTimeoutSeconds)
        if (timedOut.isEmpty()) return

        log.warn("ACK 타임아웃 차량 {}대 재배차 시도", timedOut.size)

        for (pending in timedOut) {
            try {
                retry(pending)
            } catch (e: Exception) {
                log.error("재배차 처리 중 예외 발생: dispatchId={}, err={}", pending.dispatchId, e.message, e)
            }
        }
    }

    private fun retry(pending: PendingDispatch) {
        val dispatch = dispatchRepository.findById(pending.dispatchId)
        if (dispatch == null) {
            log.warn("재배차 실패 — dispatch 없음: dispatchId={}", pending.dispatchId)
            return
        }

        val ownerId = reservationOwnerId(pending.dispatchId)
        val nextCar = dispatchableCarProjection.findNearestIdleCars(
            latitude = pending.originLatitude,
            longitude = pending.originLongitude,
        ).firstOrNull { candidate -> vehicleReservationPort.reserve(candidate.carId, ownerId) }

        if (nextCar == null) {
            log.warn("재배차 실패 — 가용 차량 없음: dispatchId={}", pending.dispatchId)
            dispatchRepository.update(dispatch.cancel())
            return
        }

        val request = dispatchRequestRepository.findById(pending.requestId)
        if (request == null) {
            vehicleReservationPort.release(nextCar.carId, ownerId)
            log.warn("재배차 실패 — request 없음: requestId={}", pending.requestId)
            return
        }

        val pickupRoute = try {
            directionsPort.findRoute(
                GeoPoint(nextCar.latitude, nextCar.longitude),
                request.origin,
            )
        } catch (e: Exception) {
            vehicleReservationPort.release(nextCar.carId, ownerId)
            log.error("재배차 경로 계산 실패: dispatchId={}, err={}", pending.dispatchId, e.message)
            dispatchRepository.update(dispatch.cancel())
            return
        }

        val newEstimatedPickupTime = ceil(pickupRoute.durationSeconds / SECONDS_PER_MINUTE).toInt()
        val reassigned = dispatch.reassign(
            newCarId = nextCar.carId,
            newCarNumber = nextCar.carNumber,
            newPickupRoutePath = pickupRoute.routePath,
            newEstimatedPickupTime = newEstimatedPickupTime,
        )

        try {
            dispatchRepository.update(reassigned)
            dispatchableCarProjection.removeCar(nextCar.carId)
        } catch (e: Exception) {
            vehicleReservationPort.release(nextCar.carId, ownerId)
            throw e
        }

        releasePreviousReservations(pending)

        pendingStore.register(
            PendingDispatch(
                dispatchId = pending.dispatchId,
                carId = nextCar.carId,
                requestId = pending.requestId,
                originLatitude = pending.originLatitude,
                originLongitude = pending.originLongitude,
            )
        )

        controlPort.sendDispatchCommand(
            carId = nextCar.carId,
            tripId = pending.dispatchId.toString(),
            route = pickupRoute.routePath,
            distanceMeters = pickupRoute.distanceMeters,
            durationSeconds = pickupRoute.durationSeconds,
            phase = "to_pickup",
        )

        log.info("재배차 완료: dispatchId={}, 이전carId={}, 새carId={}", pending.dispatchId, pending.carId, nextCar.carId)
    }

    private companion object {
        const val SECONDS_PER_MINUTE = 60.0
        fun reservationOwnerId(dispatchId: Long): String = "dispatch-retry:$dispatchId"
    }

    private fun releasePreviousReservations(pending: PendingDispatch) {
        vehicleReservationPort.release(pending.carId, "dispatch-request:${pending.requestId}")
        vehicleReservationPort.release(pending.carId, reservationOwnerId(pending.dispatchId))
    }
}
