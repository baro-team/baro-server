package com.baro.dispatch.infrastructure.persistence

import com.baro.dispatch.domain.model.Dispatch
import com.baro.dispatch.domain.repository.DispatchRepository
import org.springframework.stereotype.Repository

@Repository
class DispatchRepositoryAdapter(
    private val repo: DispatchJpaRepository,
) : DispatchRepository {
    override fun save(dispatch: Dispatch): Long =
        requireNotNull(repo.save(DispatchEntity.from(dispatch)).dispatchId) { "배차 ID 생성에 실패했습니다." }
}
