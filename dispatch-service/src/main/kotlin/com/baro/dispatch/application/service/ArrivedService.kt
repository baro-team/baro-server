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
            ?: throw IllegalArgumentException("유효하지 않은 tripId입니다. tripId=$tripId")

        val dispatch = dispatchRepository.findById(dispatchId)
            ?: throw IllegalArgumentException("배차 정보를 찾을 수 없습니다. dispatchId=$dispatchId")

        require(dispatch.carId == vehicleId.toLongOrNull()) {
            "배차된 차량과 요청 차량이 일치하지 않습니다. dispatchCarId=${dispatch.carId}, requestVehicleId=$vehicleId"
        }

        log.info("픽업 도착 확인. vehicleId={}, dispatchId={}, dropoffPoints={}", vehicleId, dispatchId, dispatch.dropoffRoutePath.size)

        controlPort.sendDispatchCommand(
            carId = dispatch.carId,
            tripId = tripId,
            route = dispatch.dropoffRoutePath,
            distanceMeters = (dispatch.estimatedRideTime * METERS_PER_MINUTE).toInt(),
            durationSeconds = dispatch.estimatedRideTime * SECONDS_PER_MINUTE,
            phase = "to_dest",
        )
    }

    private companion object {
        const val SECONDS_PER_MINUTE = 60
        const val METERS_PER_MINUTE = 400  // 대략 24km/h 기준
    }
}
