package com.baro.dispatch.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository

import com.baro.dispatch.domain.model.DispatchStatus
import jakarta.persistence.QueryHint
import org.springframework.data.jpa.repository.QueryHints
import java.time.OffsetDateTime
import java.util.Optional
import java.util.stream.Stream

interface DispatchJpaRepository : JpaRepository<DispatchEntity, Long> {
    fun findTopByCarIdAndStatusInOrderByCreatedAtDesc(
        carId: Long,
        statuses: List<DispatchStatus>,
    ): Optional<DispatchEntity>

    @QueryHints(QueryHint(name = "org.hibernate.fetchSize", value = "1000"))
    fun streamAllByCreatedAtAfter(createdAt: OffsetDateTime): Stream<DispatchEntity>
}
