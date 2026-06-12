package com.baro.dispatch.infrastructure.persistence

import com.baro.dispatch.domain.model.DispatchRequestStatus
import jakarta.persistence.QueryHint
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.QueryHints
import java.time.OffsetDateTime
import java.util.stream.Stream

interface DispatchRequestJpaRepository : JpaRepository<DispatchRequestEntity, Long> {
    @QueryHints(QueryHint(name = "org.hibernate.fetchSize", value = "1000"))
    fun streamAllByRequestedAtAfterAndStatus(
        requestedAt: OffsetDateTime,
        status: DispatchRequestStatus
    ): Stream<DispatchRequestEntity>
}
