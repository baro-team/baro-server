package com.baro.control.service

import com.baro.control.dto.VehicleState
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

@Component
class VehicleStateStore(
    meterRegistry: MeterRegistry,
) {
    private val states = ConcurrentHashMap<String, VehicleState>()
    private val emitters = CopyOnWriteArrayList<SseEmitter>()
    private val relocationRoutes = ConcurrentHashMap<String, List<Map<String, Double>>>()
    private val relocationTargets = ConcurrentHashMap<String, Map<String, Double>>()
    private val broadcastFailureCounter = Counter.builder("baro_control_sse_send_failures_total").tag("phase", "broadcast").register(meterRegistry)
    private val heartbeatFailureCounter = Counter.builder("baro_control_sse_send_failures_total").tag("phase", "heartbeat").register(meterRegistry)
    private val initialFailureCounter = Counter.builder("baro_control_sse_send_failures_total").tag("phase", "initial").register(meterRegistry)

    fun setRelocation(vehicleId: String, route: List<Map<String, Double>>, target: Map<String, Double>) {
        relocationRoutes[vehicleId] = route
        relocationTargets[vehicleId] = target
    }

    fun update(state: VehicleState) {
        val enriched = if (state.status == "relocating") {
            state.copy(
                relocationRoute = relocationRoutes[state.vehicleId],
                relocationTarget = relocationTargets[state.vehicleId],
            )
        } else {
            relocationRoutes.remove(state.vehicleId)
            relocationTargets.remove(state.vehicleId)
            state
        }
        states[state.vehicleId] = enriched
        broadcast(enriched)
    }

    fun findAll(): List<VehicleState> = states.values.toList()

    fun find(vehicleId: String): VehicleState? = states[vehicleId]

    fun subscribe(): SseEmitter {
        val emitter = SseEmitter(Long.MAX_VALUE)
        emitter.onCompletion { emitters.remove(emitter) }
        emitter.onTimeout { emitters.remove(emitter) }
        emitter.onError { emitters.remove(emitter) }

        // 초기 상태 전송 후 emitters에 추가 — broadcast와의 race condition 방지
        emitters.add(emitter)

        states.values.forEach { state ->
            try {
                synchronized(emitter) {
                    emitter.send(SseEmitter.event().name("vehicle").data(state))
                }
            } catch (e: Exception) {
                initialFailureCounter.increment()
                emitters.remove(emitter)
                emitter.completeWithError(e)
                return emitter
            }
        }

        return emitter
    }

    @Scheduled(fixedRate = 30_000)
    fun heartbeat() {
        val dead = mutableListOf<SseEmitter>()
        emitters.forEach { emitter ->
            try {
                synchronized(emitter) {
                    emitter.send(SseEmitter.event().name("ping").data(""))
                }
            } catch (e: Exception) {
                heartbeatFailureCounter.increment()
                emitter.completeWithError(e)
                dead.add(emitter)
            }
        }
        if (dead.isNotEmpty()) emitters.removeAll(dead.toSet())
    }

    private fun broadcast(state: VehicleState) {
        val dead = mutableListOf<SseEmitter>()
        emitters.forEach { emitter ->
            try {
                synchronized(emitter) {
                    emitter.send(SseEmitter.event().name("vehicle").data(state))
                }
            } catch (e: Exception) {
                broadcastFailureCounter.increment()
                emitter.completeWithError(e)
                dead.add(emitter)
            }
        }
        if (dead.isNotEmpty()) emitters.removeAll(dead.toSet())
    }
}
