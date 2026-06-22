package com.baro.dispatch.application.service

import com.baro.dispatch.application.port.out.DispatchableCarProjection
import com.baro.dispatch.infrastructure.kafka.CarStatus
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Service
open class CarStateService(
    private val dispatchableCarProjection: DispatchableCarProjection,
    private val vehicleLocationStreamService: VehicleLocationStreamService,
    private val meterRegistry: MeterRegistry,
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private fun stateEventCounter(status: String) = Counter.builder("baro_dispatch_vehicle_state_events_total")
        .tag("status", status)
        .register(meterRegistry)
    private val idleGeoSavesCounter = Counter.builder("baro_dispatch_idle_geo_saves_total").register(meterRegistry)
    private val idleGeoRemovesCounter = Counter.builder("baro_dispatch_idle_geo_removes_total").register(meterRegistry)
    private val currentStatusCounts = ConcurrentHashMap<String, AtomicInteger>()
    private val carStatuses = ConcurrentHashMap<Long, String>()

    init {
        CarStatus.entries.forEach { status ->
            Gauge.builder("baro_dispatch_vehicle_status_current", currentStatusCounts.computeIfAbsent(status.value) { AtomicInteger(0) }) { it.get().toDouble() }
                .tag("status", status.value)
                .register(meterRegistry)
        }
    }

    open fun handle(command: CarStateCommand) {
        log.debug("차량 상태 데이터를 수신했습니다. carId={}, status={}, timestamp={}", command.carId, command.status.value, command.timestamp)
        stateEventCounter(command.status.value).increment()
        updateStatusCounts(command.carId, command.status.value)
        when (command.status) {
            CarStatus.IDLE, CarStatus.RELOCATING -> {
                log.debug("배차 가능한 차량 위치를 Redis GEO에 저장합니다. carId={}, latitude={}, longitude={}", command.carId, command.latitude, command.longitude)
                dispatchableCarProjection.saveIdleCarLocation(command.carId, command.carNumber, command.latitude, command.longitude)
                idleGeoSavesCounter.increment()
            }

            CarStatus.MOVING_TO_PICKUP, CarStatus.DRIVING -> {
                log.debug("배차 가능 차량 목록에서 제외합니다. carId={}, status={}", command.carId, command.status.value)
                dispatchableCarProjection.removeCar(command.carId)
                idleGeoRemovesCounter.increment()
            }
        }

        vehicleLocationStreamService.publish(command)
    }

    private fun updateStatusCounts(carId: Long, newStatus: String) {
        val previousStatus = carStatuses.put(carId, newStatus)
        if (previousStatus == newStatus) return
        if (previousStatus != null && previousStatus != newStatus) {
            currentStatusCounts.computeIfPresent(previousStatus) { _, counter ->
                counter.updateAndGet { value -> if (value > 0) value - 1 else 0 }
                counter
            }
        }
        currentStatusCounts.computeIfAbsent(newStatus) { AtomicInteger(0) }.incrementAndGet()
    }
}

data class CarStateCommand(
    val carIdKey: String?,
    val carId: Long,
    val carNumber: String?,
    val latitude: Double,
    val longitude: Double,
    val speed: Int,
    val battery: Int,
    val heading: Double,
    val status: CarStatus,
    val timestamp: String,
)
