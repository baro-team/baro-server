package com.baro.dispatch.infrastructure.persistence

import com.baro.dispatch.domain.model.DispatchRequest
import com.baro.dispatch.domain.repository.DispatchRequestRepository
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@Repository
class InMemoryDispatchRequestRepository : DispatchRequestRepository {
    private val sequence = AtomicLong(0)
    private val requests = ConcurrentHashMap<Long, DispatchRequest>()

    override fun save(request: DispatchRequest): Long {
        val requestId = sequence.incrementAndGet()
        requests[requestId] = request
        return requestId
    }
}

