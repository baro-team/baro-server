package com.baro.dispatch.application.service

import com.baro.dispatch.application.port.out.ControlPort
import com.baro.dispatch.application.port.out.DirectionsPort
import com.baro.dispatch.application.port.out.RelocationPort
import com.baro.dispatch.domain.model.GeoPoint
import com.baro.dispatch.domain.repository.DispatchRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ArrivedService(
    private val dispatchRepository: DispatchRepository,
    private val controlPort: ControlPort,
    private val directionsPort: DirectionsPort,
    private val relocationPort: RelocationPort,
    private val vehicleLocationStreamService: VehicleLocationStreamService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun handleArrived(vehicleId: String, tripId: String, phase: String) {
        val dispatchId = tripId.toLongOrNull()
            ?: throw IllegalArgumentException("유효하지 않은 tripId입니다. tripId=$tripId")

        val dispatch = dispatchRepository.findById(dispatchId)
            ?: throw IllegalArgumentException("배차 정보를 찾을 수 없습니다. dispatchId=$dispatchId")

        require(dispatch.carId == vehicleId.toLongOrNull()) {
            "배차된 차량과 요청 차량이 일치하지 않습니다. dispatchCarId=${dispatch.carId}, requestVehicleId=$vehicleId"
        }

        when (phase) {
            "to_pickup" -> {
                log.info("픽업 도착 — 목적지 이동 명령 전송. vehicleId={}, dispatchId={}", vehicleId, dispatchId)
                vehicleLocationStreamService.publishArrival(dispatchId, dispatch.carId, phase)
                controlPort.sendDispatchCommand(
                    carId = dispatch.carId,
                    tripId = tripId,
                    route = dispatch.dropoffRoutePath,
                    distanceMeters = (dispatch.estimatedRideTime * METERS_PER_MINUTE).toInt(),
                    durationSeconds = dispatch.estimatedRideTime * SECONDS_PER_MINUTE,
                    phase = "to_dest",
                )
            }
            "to_dest" -> {
                log.info("목적지 도착 — 운행 완료. vehicleId={}, dispatchId={}", vehicleId, dispatchId)
                vehicleLocationStreamService.publishArrival(dispatchId, dispatch.carId, phase)
                dispatchRepository.update(dispatch.complete())
                triggerRelocation(dispatch.carId, dispatch.dropoffRoutePath.lastOrNull())
            }
            else -> log.warn("알 수 없는 phase. vehicleId={}, tripId={}, phase={}", vehicleId, tripId, phase)
        }
    }

    private fun triggerRelocation(carId: Long, currentLocation: GeoPoint?) {
        if (currentLocation == null) {
            log.warn("목적지 경로 정보 없음 — 재배치 생략. carId={}", carId)
            return
        }
        try {
            val dest = relocationPort.assignRelocation(carId, currentLocation.latitude, currentLocation.longitude)
            val route = directionsPort.findRoute(
                origin = currentLocation,
                destination = GeoPoint(latitude = dest.targetLat, longitude = dest.targetLon),
            )
            controlPort.sendRelocateCommand(
                carId = carId,
                route = route.routePath,
                distanceMeters = route.distanceMeters,
                durationSeconds = route.durationSeconds,
            )
            log.info("재배치 명령 전송 완료. carId={}, standId={}, points={}", carId, dest.targetStandId, route.routePath.size)
        } catch (e: Exception) {
            // 재배치 실패 시 차량은 idle 상태로 dispatch pool에 잔류 — 운행 완료에 영향 없음
            log.warn("재배치 명령 전송 실패 (차량은 idle 상태 유지). carId={}, err={}", carId, e.message)
        }
    }

    private companion object {
        const val SECONDS_PER_MINUTE = 60
        const val METERS_PER_MINUTE = 400
    }
}
