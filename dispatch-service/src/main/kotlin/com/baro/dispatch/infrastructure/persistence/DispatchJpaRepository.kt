package com.baro.dispatch.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository

import com.baro.dispatch.domain.model.DispatchStatus
import java.util.Optional

interface DispatchJpaRepository : JpaRepository<DispatchEntity, Long> {
    fun findTopByCarIdAndStatusInOrderByCreatedAtDesc(
        carId: Long,
        statuses: List<DispatchStatus>,
    ): Optional<DispatchEntity>
}
