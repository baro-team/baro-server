package com.baro.control.service

import com.baro.control.client.DispatchServiceClient
import com.baro.control.client.RelocationServiceClient
import com.baro.control.dto.EventPayload
import org.slf4j.LoggerFactory
import org.springframework.core.task.TaskExecutor
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

@Service
class EventService(
    private val dispatchClient: DispatchServiceClient,
    private val relocationClient: RelocationServiceClient,
    private val vehicleStateStore: VehicleStateStore,
    private val taskExecutor: TaskExecutor,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun handleEvent(vehicleId: String, p: EventPayload) {
        when (p.eventType) {
            "ARRIVED" -> {
                log.info("[{}] ARRIVED trip={} phase={} lat={} lon={}", vehicleId, p.tripId, p.phase, p.lat, p.lon)
                p.tripId?.let { dispatchClient.notifyArrived(vehicleId, it, p.phase ?: "to_pickup") }
                if (p.phase == "to_dest") {
                    val carId = vehicleId.toLongOrNull()
                    if (carId == null) {
                        log.error("vehicleId를 Long으로 변환 불가 — 재배치 생략. vehicleId={}", vehicleId)
                        return
                    }
                    // ARRIVED 페이로드의 lat/lon을 우선 사용; 없으면 state store에서 폴백
                    val (lat, lon) = if (p.lat != null && p.lon != null) {
                        p.lat to p.lon
                    } else {
                        val state = vehicleStateStore.find(vehicleId)
                        state?.latitude to state?.longitude
                    }
                    if (lat == null || lon == null) {
                        log.error("차량 위치 정보 없음 — 재배치 생략. vehicleId={}", vehicleId)
                        return
                    }
                    log.info("[{}] 재배치 트리거 시작. carId={} lat={} lon={}", vehicleId, carId, lat, lon)
                    try {
                        CompletableFuture.runAsync({
                            try {
                                relocationClient.notifyVehicleCompleted(carId, lat, lon)
                                log.info("[{}] 재배치 서비스 통보 완료. carId={}", vehicleId, carId)
                            } catch (e: Exception) {
                                log.error("재배치 서비스 통보 실패. carId={}", carId, e)
                            }
                        }, taskExecutor)
                    } catch (e: Exception) {
                        log.error("재배치 비동기 작업 제출 실패. carId={}", carId, e)
                    }
                }
            }
            "WARNING" -> {
                log.warn("[{}] WARNING code={} detail={}", vehicleId, p.code, p.detail)
            }
            else -> log.info("[{}] event={}", vehicleId, p.eventType)
        }
    }
}
