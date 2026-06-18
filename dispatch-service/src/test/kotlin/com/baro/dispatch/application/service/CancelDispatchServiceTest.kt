package com.baro.dispatch.application.service

import com.baro.dispatch.application.port.out.ControlPort
import com.baro.dispatch.application.port.out.DispatchableCarCandidate
import com.baro.dispatch.application.port.out.DispatchableCarProjection
import com.baro.dispatch.application.port.out.VehicleReservationPort
import com.baro.dispatch.domain.model.Dispatch
import com.baro.dispatch.domain.model.DispatchStatus
import com.baro.dispatch.domain.model.GeoPoint
import com.baro.dispatch.domain.repository.DispatchRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.time.OffsetDateTime

class CancelDispatchServiceTest {
    @Test
    fun `출발지로 이동 중인 배차를 취소한다`() {
        var updatedDispatch: Dispatch? = null
        val restoredCars = mutableListOf<RestoredCar>()
        val cancelCommands = mutableListOf<Pair<Long, String>>()
        val releasedReservations = mutableListOf<Pair<Long, String>>()
        val dispatch = dispatch(status = DispatchStatus.REQUESTED)
        val service = cancelDispatchService(
            dispatch = dispatch,
            onUpdate = { updatedDispatch = it },
            restoredCars = restoredCars,
            cancelCommands = cancelCommands,
            releasedReservations = releasedReservations,
        )

        val result = service.cancel(CancelDispatchCommand(dispatchId = 10L, userId = 2L))

        assertEquals(10L, result.dispatchId)
        assertEquals(1L, result.requestId)
        assertEquals(101L, result.carId)
        assertEquals("12가3456", result.carNumber)
        assertEquals("CANCELLED", result.status)
        assertEquals(DispatchStatus.CANCELLED, updatedDispatch?.status)
        assertEquals(listOf(RestoredCar(101L, "12가3456", 37.4, 127.1)), restoredCars)
        assertEquals(listOf(101L to "10"), cancelCommands)
        assertEquals(
            listOf(101L to "dispatch-request:1", 101L to "dispatch-retry:10"),
            releasedReservations,
        )
    }

    @Test
    fun `배차 사용자가 아니면 취소할 수 없다`() {
        val service = cancelDispatchService(dispatch = dispatch(status = DispatchStatus.REQUESTED))

        val exception = assertFailsWith<IllegalArgumentException> {
            service.cancel(CancelDispatchCommand(dispatchId = 10L, userId = 3L))
        }

        assertEquals("배차 요청 사용자와 취소 요청 사용자가 일치하지 않습니다.", exception.message)
    }

    @Test
    fun `완료된 배차는 취소할 수 없다`() {
        val service = cancelDispatchService(dispatch = dispatch(status = DispatchStatus.COMPLETED))

        val exception = assertFailsWith<IllegalArgumentException> {
            service.cancel(CancelDispatchCommand(dispatchId = 10L, userId = 2L))
        }

        assertEquals("취소할 수 없는 배차 상태입니다.", exception.message)
    }

    private fun cancelDispatchService(
        dispatch: Dispatch?,
        onUpdate: (Dispatch) -> Unit = {},
        restoredCars: MutableList<RestoredCar> = mutableListOf(),
        cancelCommands: MutableList<Pair<Long, String>> = mutableListOf(),
        releasedReservations: MutableList<Pair<Long, String>> = mutableListOf(),
    ): CancelDispatchService = CancelDispatchService(
        dispatchRepository = object : DispatchRepository {
            override fun save(dispatch: Dispatch): Long = error("사용하지 않습니다.")
            override fun update(dispatch: Dispatch) = onUpdate(dispatch)
            override fun findById(dispatchId: Long): Dispatch? = dispatch?.takeIf { dispatchId == it.id }
            override fun findActiveByCarId(carId: Long): Dispatch? = null
        },
        dispatchableCarProjection = object : DispatchableCarProjection {
            override fun saveIdleCarLocation(carId: Long, carNumber: String?, latitude: Double, longitude: Double) {
                restoredCars += RestoredCar(carId, carNumber, latitude, longitude)
            }
            override fun removeCar(carId: Long) = Unit
            override fun findNearestIdleCars(latitude: Double, longitude: Double): List<DispatchableCarCandidate> = emptyList()
        },
        vehicleReservationPort = object : VehicleReservationPort {
            override fun reserve(carId: Long, ownerId: String): Boolean = error("사용하지 않습니다.")
            override fun release(carId: Long, ownerId: String) {
                releasedReservations += carId to ownerId
            }
        },
        controlPort = object : ControlPort {
            override fun sendDispatchCommand(
                carId: Long,
                tripId: String,
                route: List<GeoPoint>,
                distanceMeters: Int,
                durationSeconds: Int,
                phase: String,
            ) = Unit
            override fun sendCancelDispatchCommand(carId: Long, tripId: String) {
                cancelCommands += carId to tripId
            }
        },
        pendingDispatchStore = PendingDispatchStore(),
        transactionTemplate = TransactionTemplate(object : PlatformTransactionManager {
            override fun getTransaction(definition: org.springframework.transaction.TransactionDefinition?) =
                org.springframework.transaction.support.SimpleTransactionStatus()
            override fun commit(status: org.springframework.transaction.TransactionStatus) = Unit
            override fun rollback(status: org.springframework.transaction.TransactionStatus) = Unit
        }),
    )

    private fun dispatch(status: DispatchStatus): Dispatch = Dispatch(
        id = 10L,
        requestId = 1L,
        userId = 2L,
        carId = 101L,
        carNumber = "12가3456",
        standId = 0L,
        createdAt = OffsetDateTime.parse("2026-04-27T00:00:00Z"),
        estimatedPickupTime = 5,
        estimatedRideTime = 20,
        pickupRoutePath = listOf(GeoPoint(longitude = 127.1, latitude = 37.4)),
        dropoffRoutePath = emptyList(),
        fare = 12_100,
        status = status,
    )

    data class RestoredCar(
        val carId: Long,
        val carNumber: String?,
        val latitude: Double,
        val longitude: Double,
    )
}
