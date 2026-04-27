package com.baro.dispatch.infrastructure.persistence

import com.baro.dispatch.domain.model.DispatchRequest
import com.baro.dispatch.domain.repository.DispatchRequestRepository
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class JdbcDispatchRequestRepository(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
) : DispatchRequestRepository {
    override fun save(request: DispatchRequest): Long {
        val params = MapSqlParameterSource()
            .addValue("userId", request.userId)
            .addValue("startX", request.origin.longitude)
            .addValue("startY", request.origin.latitude)
            .addValue("endX", request.destination.longitude)
            .addValue("endY", request.destination.latitude)
            .addValue("requestedAt", request.requestedAt)
            .addValue("status", request.status.value)
            .addValue("updatedAt", request.updatedAt)

        return jdbcTemplate.queryForObject(
            """
                INSERT INTO dispatch_request (
                    user_id,
                    start_location,
                    end_location,
                    requested_at,
                    status,
                    updated_at
                )
                VALUES (
                    :userId,
                    point(:startX, :startY),
                    point(:endX, :endY),
                    :requestedAt,
                    :status,
                    :updatedAt
                )
                RETURNING request_id
            """.trimIndent(),
            params,
            Long::class.java,
        ) ?: error("배차 요청 ID가 반환되지 않았습니다.")
    }
}
