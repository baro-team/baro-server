package com.baro.dispatch.application.service

import com.baro.dispatch.application.port.out.ControlPort
import com.baro.dispatch.application.port.out.DirectionsPort
import com.baro.dispatch.application.port.out.DispatchableCarProjection
import com.baro.dispatch.application.port.out.DispatchableCarCandidate
import com.baro.dispatch.application.port.out.RouteEstimate
import com.baro.dispatch.application.port.out.VehicleReservationPort
import com.baro.dispatch.domain.model.Dispatch
import com.baro.dispatch.domain.model.DispatchRequestStatus
import com.baro.dispatch.domain.model.GeoPoint
import com.baro.dispatch.domain.repository.DispatchRepository
import com.baro.dispatch.domain.repository.DispatchRequestRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.time.Clock
import java.time.Duration
import java.time.OffsetDateTime
import kotlin.math.ceil

@Service
class ConfirmDispatchService(
    private val dispatchRequestRepository: DispatchRequestRepository,
    private val dispatchRepository: DispatchRepository,
    private val dispatchableCarProjection: DispatchableCarProjection,
    private val vehicleReservationPort: VehicleReservationPort,
    private val directionsPort: DirectionsPort,
    private val controlPort: ControlPort,
    private val pendingDispatchStore: PendingDispatchStore,
    private val transactionTemplate: TransactionTemplate,
    private val clock: Clock,
) {
    fun confirm(command: ConfirmDispatchCommand): ConfirmDispatchResult {
        val now = OffsetDateTime.now(clock)
        val preDispatchRequest = dispatchRequestRepository.findById(command.requestId)
            ?: throw IllegalArgumentException("PRE배차 요청을 찾을 수 없습니다.")

        require(preDispatchRequest.userId == command.userId) { "PRE배차 요청 사용자와 배차 요청 사용자가 일치하지 않습니다." }
        require(preDispatchRequest.status == DispatchRequestStatus.PENDING) { "이미 처리된 PRE배차 요청입니다." }
        require(!preDispatchRequest.isExpired(now)) { "만료된 PRE배차 요청입니다." }

        val dispatchableCar = reserveNearestIdleCar(
            latitude = preDispatchRequest.origin.latitude,
            longitude = preDispatchRequest.origin.longitude,
            ownerId = reservationOwnerId(command.requestId),
        ) ?: throw IllegalArgumentException("배차 가능한 차량을 찾을 수 없습니다.")

        val ownerId = reservationOwnerId(command.requestId)
        val pickupRoute = try {
            // 트랜잭션 외부에서 외부 API 호출 (커넥션 점유 최소화)
            val carLocation = GeoPoint(latitude = dispatchableCar.latitude, longitude = dispatchableCar.longitude)
            directionsPort.findRoute(carLocation, preDispatchRequest.origin)
        } catch (exception: Exception) {
            vehicleReservationPort.release(dispatchableCar.carId, ownerId)
            throw exception
        }
        val estimatedPickupTime = ceil(pickupRoute.durationSeconds / SECONDS_PER_MINUTE).toInt()

        val dispatchId = try {
            saveDispatch(
                command = command,
                now = now,
                carId = dispatchableCar.carId,
                carNumber = dispatchableCar.carNumber,
                estimatedPickupTime = estimatedPickupTime,
                pickupRoute = pickupRoute,
                preDispatchRequest = preDispatchRequest,
            )
        } catch (exception: Exception) {
            vehicleReservationPort.release(dispatchableCar.carId, ownerId)
            throw exception
        }

        dispatchableCarProjection.removeCar(dispatchableCar.carId)

        pendingDispatchStore.register(
            PendingDispatch(
                dispatchId = dispatchId,
                carId = dispatchableCar.carId,
                requestId = command.requestId,
                originLatitude = preDispatchRequest.origin.latitude,
                originLongitude = preDispatchRequest.origin.longitude,
            )
        )

        // DB 커밋 및 pending 등록 후 외부 명령 전송 (빠른 ACK 유실 방지)
        controlPort.sendDispatchCommand(
            carId = dispatchableCar.carId,
            tripId = dispatchId.toString(),
            route = pickupRoute.routePath,
            distanceMeters = pickupRoute.distanceMeters,
            durationSeconds = pickupRoute.durationSeconds,
            phase = "to_pickup",
        )

        return ConfirmDispatchResult(
            dispatchId = dispatchId,
            requestId = command.requestId,
            userId = command.userId,
            carId = dispatchableCar.carId,
            carNumber = dispatchableCar.carNumber,
            standId = TEMPORARY_STAND_ID,
            estimatedPickupTime = estimatedPickupTime,
            estimatedRideTime = preDispatchRequest.estimatedTime,
            pickupRoutePath = pickupRoute.routePath,
            dropoffRoutePath = preDispatchRequest.routePath,
            fare = preDispatchRequest.fare,
            status = Dispatch.requested(
                requestId = command.requestId, userId = command.userId,
                carId = dispatchableCar.carId, carNumber = dispatchableCar.carNumber, standId = TEMPORARY_STAND_ID,
                createdAt = now, estimatedPickupTime = estimatedPickupTime,
                estimatedRideTime = preDispatchRequest.estimatedTime,
                pickupRoutePath = pickupRoute.routePath,
                dropoffRoutePath = preDispatchRequest.routePath,
                fare = preDispatchRequest.fare,
            ).status.name,
        )
    }

    fun saveDispatch(
        command: ConfirmDispatchCommand,
        now: OffsetDateTime,
        carId: Long,
        carNumber: String?,
        estimatedPickupTime: Int,
        pickupRoute: RouteEstimate,
        preDispatchRequest: com.baro.dispatch.domain.model.DispatchRequest,
    ): Long = transactionTemplate.execute {
        val dispatch = Dispatch.requested(
            requestId = command.requestId,
            userId = command.userId,
            carId = carId,
            carNumber = carNumber,
            standId = TEMPORARY_STAND_ID,
            createdAt = now,
            estimatedPickupTime = estimatedPickupTime,
            estimatedRideTime = preDispatchRequest.estimatedTime,
            pickupRoutePath = pickupRoute.routePath,
            dropoffRoutePath = preDispatchRequest.routePath,
            fare = preDispatchRequest.fare,
        )
        val dispatchId = dispatchRepository.save(dispatch)
        dispatchRequestRepository.save(preDispatchRequest.markMatched(now))
        dispatchId
    }!!

    private fun reserveNearestIdleCar(latitude: Double, longitude: Double, ownerId: String): DispatchableCarCandidate? =
        dispatchableCarProjection.findNearestIdleCars(latitude, longitude)
            .firstOrNull { candidate -> vehicleReservationPort.reserve(candidate.carId, ownerId) }

    private companion object {
        const val TEMPORARY_STAND_ID = 0L
        const val SECONDS_PER_MINUTE = 60.0
        val PRE_DISPATCH_EXPIRATION: Duration = Duration.ofMinutes(10)

        fun reservationOwnerId(requestId: Long): String = "dispatch-request:$requestId"

        fun com.baro.dispatch.domain.model.DispatchRequest.isExpired(now: OffsetDateTime): Boolean =
            requestedAt.plus(PRE_DISPATCH_EXPIRATION).isBefore(now)
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
    val carNumber: String?,
    val standId: Long,
    val estimatedPickupTime: Int,
    val estimatedRideTime: Int,
    val pickupRoutePath: List<GeoPoint>,
    val dropoffRoutePath: List<GeoPoint>,
    val fare: Int,
    val status: String,
)
