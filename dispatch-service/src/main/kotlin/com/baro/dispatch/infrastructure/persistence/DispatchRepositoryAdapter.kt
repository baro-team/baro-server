package com.baro.dispatch.infrastructure.persistence

import com.baro.dispatch.domain.model.Dispatch
import com.baro.dispatch.domain.model.DispatchStatus
import com.baro.dispatch.domain.repository.DispatchRepository
import org.springframework.stereotype.Repository

@Repository
class DispatchRepositoryAdapter(
    private val repo: DispatchJpaRepository,
) : DispatchRepository {
    override fun save(dispatch: Dispatch): Long =
        requireNotNull(repo.save(DispatchEntity.from(dispatch)).dispatchId) { "배차 ID 생성에 실패했습니다." }

    override fun update(dispatch: Dispatch) {
        val dispatchId = requireNotNull(dispatch.id) { "Dispatch ID must not be null for update" }
        val entity = repo.findById(dispatchId).orElseThrow {
            NoSuchElementException("Dispatch not found with ID: $dispatchId")
        }
        entity.status = dispatch.status
        entity.carId = dispatch.carId
        entity.carNumber = dispatch.carNumber
        entity.estimatedPickupTime = dispatch.estimatedPickupTime
        entity.pickupRoutePath = dispatch.pickupRoutePath.map { listOf(it.longitude, it.latitude) }
        repo.save(entity)
    }

    override fun findById(dispatchId: Long): Dispatch? =
        repo.findById(dispatchId).map { it.toDomain() }.orElse(null)

    override fun findActiveByCarId(carId: Long): Dispatch? =
        repo.findTopByCarIdAndStatusInOrderByCreatedAtDesc(
            carId,
            listOf(DispatchStatus.REQUESTED, DispatchStatus.ASSIGNED),
        ).map { it.toDomain() }.orElse(null)
}
