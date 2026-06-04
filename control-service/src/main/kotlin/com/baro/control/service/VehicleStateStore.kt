package com.baro.control.service

import com.baro.control.dto.VehicleState
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

@Component
class VehicleStateStore {
    private val states = ConcurrentHashMap<String, VehicleState>()
    private val emitters = CopyOnWriteArrayList<SseEmitter>()

    fun update(state: VehicleState) {
        states[state.vehicleId] = state
        broadcast(state)
    }

    fun findAll(): List<VehicleState> = states.values.toList()

    fun subscribe(): SseEmitter {
        val emitter = SseEmitter(Long.MAX_VALUE)
        emitters.add(emitter)
        emitter.onCompletion { emitters.remove(emitter) }
        emitter.onTimeout { emitters.remove(emitter) }
        emitter.onError { emitters.remove(emitter) }

        states.values.forEach { state ->
            try {
                synchronized(emitter) {
                    emitter.send(SseEmitter.event().name("vehicle").data(state))
                }
            } catch (e: Exception) {
                emitters.remove(emitter)
                emitter.completeWithError(e)
                return emitter
            }
        }
        return emitter
    }

    private fun broadcast(state: VehicleState) {
        val dead = mutableListOf<SseEmitter>()
        emitters.forEach { emitter ->
            try {
                synchronized(emitter) {
                    emitter.send(SseEmitter.event().name("vehicle").data(state))
                }
            } catch (e: Exception) {
                emitter.completeWithError(e)
                dead.add(emitter)
            }
        }
        if (dead.isNotEmpty()) {
            emitters.removeAll(dead.toSet())
        }
    }
}
