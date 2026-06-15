package com.baro.dispatch.application.service

import com.baro.dispatch.application.port.out.ControlPort
import com.baro.dispatch.application.port.out.DispatchableCarProjection
import com.baro.dispatch.domain.model.Dispatch
import com.baro.dispatch.domain.model.DispatchStatus
import com.baro.dispatch.domain.repository.DispatchRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate

@Service
class CancelDispatchService(
    private val dispatchRepository: DispatchRepository,
    private val dispatchableCarProjection: DispatchableCarProjection,
    private val controlPort: ControlPort,
    private val pendingDispatchStore: PendingDispatchStore,
    private val transactionTemplate: TransactionTemplate,
) {
    fun cancel(command: CancelDispatchCommand): CancelDispatchResult {
        val dispatch = dispatchRepository.findById(command.dispatchId)
            ?: throw IllegalArgumentException("배차 정보를 찾을 수 없습니다.")

        require(dispatch.userId == command.userId) { "배차 요청 사용자와 취소 요청 사용자가 일치하지 않습니다." }
        require(dispatch.status in CANCELLABLE_STATUSES) { "취소할 수 없는 배차 상태입니다." }

        val cancelledDispatch = transactionTemplate.execute {
            val cancelled = dispatch.cancel()
            dispatchRepository.update(cancelled)
            pendingDispatchStore.cancel(command.dispatchId)
            restoreCarToDispatchableProjection(dispatch)
            cancelled
        }!!

        controlPort.sendCancelDispatchCommand(
            carId = cancelledDispatch.carId,
            tripId = command.dispatchId.toString(),
        )

        return CancelDispatchResult(
            dispatchId = requireNotNull(cancelledDispatch.id),
            requestId = cancelledDispatch.requestId,
            carId = cancelledDispatch.carId,
            carNumber = cancelledDispatch.carNumber,
            status = cancelledDispatch.status.name,
        )
    }

    private fun restoreCarToDispatchableProjection(dispatch: Dispatch) {
        val lastKnownPoint = dispatch.pickupRoutePath.firstOrNull() ?: return

        dispatchableCarProjection.saveIdleCarLocation(
            carId = dispatch.carId,
            carNumber = dispatch.carNumber,
            latitude = lastKnownPoint.latitude,
            longitude = lastKnownPoint.longitude,
        )
    }

    private companion object {
        val CANCELLABLE_STATUSES = setOf(DispatchStatus.REQUESTED, DispatchStatus.ASSIGNED)
    }
}

data class CancelDispatchCommand(
    val dispatchId: Long,
    val userId: Long,
) {
    init {
        require(dispatchId > 0) { "배차 ID는 양수여야 합니다." }
        require(userId > 0) { "사용자 ID는 양수여야 합니다." }
    }
}

data class CancelDispatchResult(
    val dispatchId: Long,
    val requestId: Long,
    val carId: Long,
    val carNumber: String?,
    val status: String,
)
