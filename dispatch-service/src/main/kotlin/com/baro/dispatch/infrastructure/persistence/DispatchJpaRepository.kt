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

    @org.springframework.data.jpa.repository.Query("""
        SELECT new com.baro.dispatch.application.service.DispatchExportDto(
            d.createdAt, d.requestId, d.userId, 
            dr.startLatitude, dr.startLongitude, 
            dr.endLatitude, dr.endLongitude, d.status
        )
        FROM DispatchEntity d
        JOIN DispatchRequestEntity dr ON d.requestId = dr.requestId
        WHERE d.createdAt >= :start AND d.createdAt < :end
    """)
    @QueryHints(QueryHint(name = "org.hibernate.fetchSize", value = "1000"))
    fun streamAllByCreatedAtBetween(
        @org.springframework.data.repository.query.Param("start") start: OffsetDateTime, 
        @org.springframework.data.repository.query.Param("end") end: OffsetDateTime
    ): Stream<com.baro.dispatch.application.service.DispatchExportDto>
}
