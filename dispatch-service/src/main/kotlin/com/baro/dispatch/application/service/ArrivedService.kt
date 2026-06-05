package com.baro.dispatch.application.service

import com.baro.dispatch.application.port.out.ControlPort
import com.baro.dispatch.domain.repository.DispatchRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ArrivedService(
    private val dispatchRepository: DispatchRepository,
    private val controlPort: ControlPort,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun handleArrived(vehicleId: String, tripId: String) {
        val dispatchId = tripId.toLongOrNull()
            ?: run { log.warn("tripId를 Long으로 변환할 수 없습니다. tripId={}", tripId); return }

        val dispatch = dispatchRepository.findById(dispatchId)
            ?: run { log.warn("배차 정보를 찾을 수 없습니다. dispatchId={}", dispatchId); return }

        log.info("픽업 도착 확인. vehicleId={}, dispatchId={}, dropoffPoints={}", vehicleId, dispatchId, dispatch.dropoffRoutePath.size)

        val durationSeconds = dispatch.estimatedRideTime * SECONDS_PER_MINUTE
        val distanceMeters = (dispatch.fare / FARE_PER_METER).toInt()

        controlPort.sendDispatchCommand(
            carId = dispatch.carId,
            tripId = tripId,
            route = dispatch.dropoffRoutePath,
            distanceMeters = distanceMeters,
            durationSeconds = durationSeconds,
            phase = "to_dest",
        )
    }

    private companion object {
        const val SECONDS_PER_MINUTE = 60
        const val FARE_PER_METER = 0.067  // rough estimate: 67원/m
    }
}
