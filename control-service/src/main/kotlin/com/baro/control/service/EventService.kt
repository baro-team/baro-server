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
                log.info("[{}] ARRIVED trip={} phase={}", vehicleId, p.tripId, p.phase)
                p.tripId?.let { dispatchClient.notifyArrived(vehicleId, it, p.phase ?: "to_pickup") }
                if (p.phase == "to_dest") {
                    val carId = vehicleId.toLongOrNull()
                    val state = vehicleStateStore.find(vehicleId)
                    if (carId != null && state != null) {
                        try {
                            CompletableFuture.runAsync({
                                try {
                                    relocationClient.notifyVehicleCompleted(carId, state.latitude, state.longitude)
                                } catch (e: Exception) {
                                    log.warn("재배치 서비스 통보 실패 (무시). carId={}", carId, e)
                                }
                            }, taskExecutor)
                        } catch (e: Exception) {
                            log.error("재배치 비동기 작업 제출 실패. carId={}", carId, e)
                        }
                    } else {
                        log.warn("차량 상태를 찾을 수 없어 재배치 생략. vehicleId={}", vehicleId)
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
