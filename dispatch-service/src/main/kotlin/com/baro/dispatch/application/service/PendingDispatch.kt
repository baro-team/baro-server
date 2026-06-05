package com.baro.dispatch.application.service

import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import org.springframework.stereotype.Component

data class PendingDispatch(
    val dispatchId: Long,
    val carId: Long,
    val requestId: Long,
    val originLatitude: Double,
    val originLongitude: Double,
    val sentAt: Instant = Instant.now(),
)

@Component
class PendingDispatchStore {
    private val store = ConcurrentHashMap<Long, PendingDispatch>()

    fun register(pending: PendingDispatch) {
        store[pending.dispatchId] = pending
    }

    fun acknowledge(dispatchId: Long) {
        store.remove(dispatchId)
    }

    fun pollTimedOut(timeoutSeconds: Long): List<PendingDispatch> {
        val threshold = Instant.now().minusSeconds(timeoutSeconds)
        val timedOut = store.values.filter { it.sentAt.isBefore(threshold) }
        timedOut.forEach { store.remove(it.dispatchId) }
        return timedOut
    }
}
