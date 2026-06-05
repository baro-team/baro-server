package com.baro.dispatch.domain.repository

import com.baro.dispatch.domain.model.Dispatch

interface DispatchRepository {
    fun save(dispatch: Dispatch): Long
    fun findById(dispatchId: Long): Dispatch?
}
