package com.baro.dispatch.application.service

import com.baro.dispatch.infrastructure.kafka.CarStatus
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException
import jakarta.annotation.PreDestroy
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Service
class VehicleLocationStreamService {
    private val emittersByDispatchId = ConcurrentHashMap<Long, MutableSet<SseEmitter>>()
    private val latestEventsByCarId = ConcurrentHashMap<Long, VehicleLocationEvent>()
    private val activeDispatchByCarId = ConcurrentHashMap<Long, Long>() // carId → dispatchId
    private val dispatchToCarId = ConcurrentHashMap<Long, Long>()       // dispatchId → carId
    private val heartbeatExecutor = Executors.newSingleThreadScheduledExecutor { runnable ->
        Thread(runnable, "vehicle-location-sse-heartbeat").apply { isDaemon = true }
    }

    init {
        heartbeatExecutor.scheduleAtFixedRate(::sendHeartbeat, HEARTBEAT_INTERVAL_SECONDS, HEARTBEAT_INTERVAL_SECONDS, TimeUnit.SECONDS)
    }

    fun subscribe(dispatchId: Long, carId: Long): SseEmitter {
        activeDispatchByCarId[carId] = dispatchId
        dispatchToCarId[dispatchId] = carId
        val emitter = SseEmitter(STREAM_TIMEOUT_MILLIS)
        emittersByDispatchId.compute(dispatchId) { _, existingEmitters ->
            val emitters = existingEmitters ?: ConcurrentHashMap.newKeySet()
            emitters.add(emitter)
            emitters
        }
        cleanupOnTerminate(dispatchId, emitter)
        sendEvent(dispatchId, emitter, "connected", mapOf("dispatch_id" to dispatchId, "car_id" to carId))
        latestEventsByCarId[carId]?.let { latestEvent ->
            sendEvent(dispatchId, emitter, "vehicle-location", latestEvent.copy(dispatchId = dispatchId))
        }
        return emitter
    }

    fun publish(command: CarStateCommand) {
        val dispatchId = activeDispatchByCarId[command.carId] ?: return
        val payload = VehicleLocationEvent.from(dispatchId, command)
        latestEventsByCarId[command.carId] = payload
        publish(dispatchId, payload)
    }

    fun publishArrival(dispatchId: Long, carId: Long, phase: String) {
        val previous = latestEventsByCarId[carId] ?: return
        publish(
            dispatchId,
            previous.copy(
                dispatchId = dispatchId,
                status = if (phase == "to_dest") "arrived_destination" else "arrived_pickup",
                phase = phase,
            ),
        )
        if (phase == "to_dest") {
            latestEventsByCarId.remove(carId)
        }
    }

    private fun publish(dispatchId: Long, payload: VehicleLocationEvent) {
        emittersByDispatchId[dispatchId]?.toList()?.forEach { emitter ->
            sendEvent(dispatchId, emitter, "vehicle-location", payload)
        }
    }

    private fun sendHeartbeat() {
        emittersByDispatchId.forEach { (dispatchId, emitters) ->
            emitters.toList().forEach { emitter ->
                sendEvent(dispatchId, emitter, "ping", emptyMap<String, String>())
            }
        }
    }

    private fun sendEvent(dispatchId: Long, emitter: SseEmitter, eventName: String, payload: Any) {
        synchronized(emitter) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(payload))
            } catch (exception: IOException) {
                completeWithError(dispatchId, emitter, exception)
            } catch (exception: IllegalStateException) {
                completeWithError(dispatchId, emitter, exception)
            }
        }
    }

    private fun completeWithError(dispatchId: Long, emitter: SseEmitter, exception: Throwable) {
        removeEmitter(dispatchId, emitter)
        try {
            emitter.completeWithError(exception)
        } catch (_: IllegalStateException) {
            // already completed
        }
    }

    private fun cleanupOnTerminate(dispatchId: Long, emitter: SseEmitter) {
        val cleanup = { removeEmitter(dispatchId, emitter) }
        emitter.onCompletion(cleanup)
        emitter.onTimeout(cleanup)
        emitter.onError { _: Throwable -> cleanup() }
    }

    private fun removeEmitter(dispatchId: Long, emitter: SseEmitter) {
        var isEmpty = false
        emittersByDispatchId.computeIfPresent(dispatchId) { _, emitters ->
            emitters.remove(emitter)
            if (emitters.isEmpty()) {
                isEmpty = true
                null
            } else emitters
        }
        if (isEmpty) {
            dispatchToCarId.remove(dispatchId)?.let { carId ->
                activeDispatchByCarId.remove(carId, dispatchId)
            }
        }
    }

    @PreDestroy
    fun shutdown() {
        heartbeatExecutor.shutdownNow()
    }

    private companion object {
        const val STREAM_TIMEOUT_MILLIS = 10 * 60 * 1000L
        const val HEARTBEAT_INTERVAL_SECONDS = 25L
    }
}

data class VehicleLocationEvent(
    @JsonProperty("dispatch_id") val dispatchId: Long,
    @JsonProperty("car_id") val carId: Long,
    @JsonProperty("car_number") val carNumber: String?,
    val latitude: Double,
    val longitude: Double,
    val speed: Int,
    val heading: Double,
    val status: String,
    val timestamp: String,
    val phase: String,
) {
    companion object {
        fun from(dispatchId: Long, command: CarStateCommand): VehicleLocationEvent = VehicleLocationEvent(
            dispatchId = dispatchId,
            carId = command.carId,
            carNumber = command.carNumber,
            latitude = command.latitude,
            longitude = command.longitude,
            speed = command.speed,
            heading = command.heading,
            status = command.status.value,
            timestamp = command.timestamp,
            phase = if (command.status == CarStatus.DRIVING) "to_dest" else "to_pickup",
        )
    }
}
