package com.baro.dispatch.infrastructure.persistence

import com.baro.dispatch.domain.model.DispatchRequest
import com.baro.dispatch.domain.repository.DispatchRequestRepository
import org.springframework.stereotype.Repository

@Repository
class DispatchRequestRepositoryAdapter(
    private val repo: DispatchRequestJpaRepository,
) : DispatchRequestRepository {
    override fun save(request: DispatchRequest): Long =
        requireNotNull(repo.save(DispatchRequestEntity.from(request)).requestId) { "배차 요청 ID 생성에 실패했습니다." }

    override fun findById(requestId: Long): DispatchRequest? = repo.findById(requestId).map { it.toDomain() }.orElse(null)
}
