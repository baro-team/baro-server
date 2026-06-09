package com.baro.dispatch.application.service

import com.baro.dispatch.application.port.out.ControlPort
import com.baro.dispatch.application.port.out.DirectionsPort
import com.baro.dispatch.application.port.out.DispatchableCarProjection
<<<<<<< HEAD
import com.baro.dispatch.domain.model.GeoPoint
=======
>>>>>>> origin/main
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
<<<<<<< HEAD
            try {
                retry(pending)
            } catch (e: Exception) {
                log.error("재배차 처리 중 예외 발생: dispatchId={}, err={}", pending.dispatchId, e.message, e)
            }
=======
            retry(pending)
>>>>>>> origin/main
        }
    }

    private fun retry(pending: PendingDispatch) {
        val dispatch = dispatchRepository.findById(pending.dispatchId)
        if (dispatch == null) {
            log.warn("재배차 실패 — dispatch 없음: dispatchId={}", pending.dispatchId)
            return
        }

        val nextCar = dispatchableCarProjection.findNearestIdleCar(
            latitude = pending.originLatitude,
            longitude = pending.originLongitude,
        )

        if (nextCar == null) {
            log.warn("재배차 실패 — 가용 차량 없음: dispatchId={}", pending.dispatchId)
            dispatchRepository.update(dispatch.cancel())
            return
        }

        val request = dispatchRequestRepository.findById(pending.requestId)
        if (request == null) {
            log.warn("재배차 실패 — request 없음: requestId={}", pending.requestId)
            return
        }

        val pickupRoute = try {
            directionsPort.findRoute(
<<<<<<< HEAD
                GeoPoint(nextCar.latitude, nextCar.longitude),
=======
                com.baro.dispatch.domain.model.GeoPoint(nextCar.latitude, nextCar.longitude),
>>>>>>> origin/main
                request.origin,
            )
        } catch (e: Exception) {
            log.error("재배차 경로 계산 실패: dispatchId={}, err={}", pending.dispatchId, e.message)
            dispatchRepository.update(dispatch.cancel())
            return
        }

        val newEstimatedPickupTime = ceil(pickupRoute.durationSeconds / SECONDS_PER_MINUTE).toInt()
        val reassigned = dispatch.reassign(
            newCarId = nextCar.carId,
            newPickupRoutePath = pickupRoute.routePath,
            newEstimatedPickupTime = newEstimatedPickupTime,
        )

        dispatchableCarProjection.removeCar(nextCar.carId)
        dispatchRepository.update(reassigned)

        controlPort.sendDispatchCommand(
            carId = nextCar.carId,
            tripId = pending.dispatchId.toString(),
            route = pickupRoute.routePath,
            distanceMeters = pickupRoute.distanceMeters,
            durationSeconds = pickupRoute.durationSeconds,
            phase = "to_pickup",
        )

        pendingStore.register(
            PendingDispatch(
                dispatchId = pending.dispatchId,
                carId = nextCar.carId,
                requestId = pending.requestId,
                originLatitude = pending.originLatitude,
                originLongitude = pending.originLongitude,
            )
        )

        log.info("재배차 완료: dispatchId={}, 이전carId={}, 새carId={}", pending.dispatchId, pending.carId, nextCar.carId)
    }

    private companion object {
        const val SECONDS_PER_MINUTE = 60.0
    }
}
