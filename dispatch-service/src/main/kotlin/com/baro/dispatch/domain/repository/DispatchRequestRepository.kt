package com.baro.dispatch.domain.repository

import com.baro.dispatch.domain.model.DispatchRequest

interface DispatchRequestRepository {
    fun save(request: DispatchRequest): Long

    fun findById(requestId: Long): DispatchRequest?
}
