package com.baro.dispatch.application.service

import com.baro.dispatch.domain.model.Dispatch
import com.baro.dispatch.domain.model.GeoPoint
import com.baro.dispatch.domain.repository.DispatchRepository
import com.baro.dispatch.domain.repository.DispatchRequestRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.OffsetDateTime

@Service
class ConfirmDispatchService(
    private val dispatchRequestRepository: DispatchRequestRepository,
    private val dispatchRepository: DispatchRepository,
    private val clock: Clock,
) {
    @Transactional
    fun confirm(command: ConfirmDispatchCommand): ConfirmDispatchResult {
        val preDispatchRequest = dispatchRequestRepository.findById(command.requestId)
            ?: throw IllegalArgumentException("PRE배차 요청을 찾을 수 없습니다.")

        require(preDispatchRequest.userId == command.userId) { "PRE배차 요청 사용자와 배차 요청 사용자가 일치하지 않습니다." }

        // TODO: 차량/승차장 조회 및 배정 로직을 control-service 연동 또는 별도 포트로 구현한다.
        val temporaryCarId = TEMPORARY_CAR_ID
        val temporaryStandId = TEMPORARY_STAND_ID
        val temporaryPickupRoutePath = emptyList<GeoPoint>()
        val temporaryEstimatedPickupTime = 0

        val dispatch = Dispatch.requested(
            requestId = command.requestId,
            userId = command.userId,
            carId = temporaryCarId,
            standId = temporaryStandId,
            createdAt = OffsetDateTime.now(clock),
            estimatedPickupTime = temporaryEstimatedPickupTime,
            estimatedRideTime = preDispatchRequest.estimatedTime,
            pickupRoutePath = temporaryPickupRoutePath,
            dropoffRoutePath = preDispatchRequest.routePath,
            fare = preDispatchRequest.fare,
        )
        val dispatchId = dispatchRepository.save(dispatch)

        return ConfirmDispatchResult(
            dispatchId = dispatchId,
            requestId = command.requestId,
            userId = command.userId,
            carId = temporaryCarId,
            standId = temporaryStandId,
            estimatedPickupTime = temporaryEstimatedPickupTime,
            estimatedRideTime = preDispatchRequest.estimatedTime,
            pickupRoutePath = temporaryPickupRoutePath,
            dropoffRoutePath = preDispatchRequest.routePath,
            fare = preDispatchRequest.fare,
            status = dispatch.status.name,
        )
    }

    private companion object {
        const val TEMPORARY_CAR_ID = 0L
        const val TEMPORARY_STAND_ID = 0L
    }
}

data class ConfirmDispatchCommand(
    val requestId: Long,
    val userId: Long,
) {
    init {
        require(requestId > 0) { "PRE배차 요청 ID는 양수여야 합니다." }
        require(userId > 0) { "사용자 ID는 양수여야 합니다." }
    }
}

data class ConfirmDispatchResult(
    val dispatchId: Long,
    val requestId: Long,
    val userId: Long,
    val carId: Long,
    val standId: Long,
    val estimatedPickupTime: Int,
    val estimatedRideTime: Int,
    val pickupRoutePath: List<GeoPoint>,
    val dropoffRoutePath: List<GeoPoint>,
    val fare: Int,
    val status: String,
)
