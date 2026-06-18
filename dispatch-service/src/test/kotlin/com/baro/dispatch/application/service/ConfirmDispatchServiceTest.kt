package com.baro.dispatch.application.service

import com.baro.dispatch.application.port.out.ControlPort
import com.baro.dispatch.application.port.out.DirectionsPort
import com.baro.dispatch.application.port.out.DispatchableCarCandidate
import com.baro.dispatch.application.port.out.DispatchableCarProjection
import com.baro.dispatch.application.port.out.RouteEstimate
import com.baro.dispatch.application.port.out.VehicleReservationPort
import com.baro.dispatch.domain.model.Dispatch
import com.baro.dispatch.domain.model.DispatchRequest
import com.baro.dispatch.domain.model.DispatchRequestStatus
import com.baro.dispatch.domain.model.GeoPoint
import com.baro.dispatch.domain.repository.DispatchRepository
import com.baro.dispatch.domain.repository.DispatchRequestRepository
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.transaction.support.TransactionCallback
import org.springframework.transaction.PlatformTransactionManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ConfirmDispatchServiceTest {
    @Test
    fun `PRE배차 요청 ID로 실제 배차 요청을 생성한다`() {
        var savedDispatch: Dispatch? = null
        var updatedPreRequest: DispatchRequest? = null
        val removedCars = mutableListOf<Long>()
        val vehicleReservationPort = vehicleReservationPortWithReservationResult(true)
        val preRequest = DispatchRequest.pending(
            userId = 2L,
            origin = GeoPoint(longitude = 127.1, latitude = 37.4),
            destination = GeoPoint(longitude = 127.2, latitude = 37.5),
            fare = 12_100,
            routePath = listOf(GeoPoint(longitude = 127.11, latitude = 37.41)),
            estimatedTime = 46,
            distanceKm = 13.8,
            now = OffsetDateTime.ofInstant(Instant.parse("2026-04-27T00:00:00Z"), ZoneOffset.UTC),
        ).copy(id = 1L)
        val service = ConfirmDispatchService(
            dispatchRequestRepository = object : DispatchRequestRepository {
                override fun save(request: DispatchRequest): Long {
                    updatedPreRequest = request
                    return requireNotNull(request.id)
                }
                override fun findById(requestId: Long): DispatchRequest? = preRequest.takeIf { requestId == 1L }
            },
            dispatchRepository = object : DispatchRepository {
                override fun save(dispatch: Dispatch): Long {
                    savedDispatch = dispatch
                    return 10L
                }
                override fun findById(dispatchId: Long): Dispatch? = null
                override fun update(dispatch: Dispatch) = Unit
                override fun findActiveByCarId(carId: Long): Dispatch? = null
            },
            dispatchableCarProjection = dispatchableCarProjectionWith(101L, removedCars),
            vehicleReservationPort = vehicleReservationPort,
            directionsPort = noopDirectionsPort(),
            controlPort = noopControlPort(),
            pendingDispatchStore = PendingDispatchStore(),
            transactionTemplate = TransactionTemplate(object : PlatformTransactionManager {
                override fun getTransaction(definition: org.springframework.transaction.TransactionDefinition?) =
                    org.springframework.transaction.support.SimpleTransactionStatus()
                override fun commit(status: org.springframework.transaction.TransactionStatus) = Unit
                override fun rollback(status: org.springframework.transaction.TransactionStatus) = Unit
            }),
            clock = Clock.fixed(Instant.parse("2026-04-27T00:05:00Z"), ZoneOffset.UTC),
        )

        val result = service.confirm(ConfirmDispatchCommand(requestId = 1L, userId = 2L))

        assertEquals(10L, result.dispatchId)
        assertEquals(1L, result.requestId)
        assertEquals(2L, result.userId)
        assertEquals(101L, result.carId)
        assertEquals(0L, result.standId)
        assertEquals(0, result.estimatedPickupTime)
        assertEquals(46, result.estimatedRideTime)
        assertEquals(emptyList(), result.pickupRoutePath)
        assertEquals(preRequest.routePath, result.dropoffRoutePath)
        assertEquals(12_100, result.fare)
        assertEquals("REQUESTED", result.status)

        val dispatch = requireNotNull(savedDispatch)
        assertEquals(Instant.parse("2026-04-27T00:05:00Z"), dispatch.createdAt.toInstant())
        assertEquals(preRequest.routePath, dispatch.dropoffRoutePath)
        assertEquals(DispatchRequestStatus.MATCHED, requireNotNull(updatedPreRequest).status)
        assertEquals(Instant.parse("2026-04-27T00:05:00Z"), requireNotNull(updatedPreRequest).updatedAt.toInstant())
        assertEquals(listOf(101L), removedCars)
    }

    @Test
    fun `PRE배차 요청이 없으면 예외를 던진다`() {
        val service = ConfirmDispatchService(
            dispatchRequestRepository = object : DispatchRequestRepository {
                override fun save(request: DispatchRequest): Long = error("사용하지 않습니다.")
                override fun findById(requestId: Long): DispatchRequest? = null
            },
            dispatchRepository = object : DispatchRepository {
                override fun save(dispatch: Dispatch): Long = error("사용하지 않습니다.")
                override fun findById(dispatchId: Long): Dispatch? = null
                override fun update(dispatch: Dispatch) = Unit
                override fun findActiveByCarId(carId: Long): Dispatch? = null
            },
            dispatchableCarProjection = dispatchableCarProjectionWith(101L),
            vehicleReservationPort = vehicleReservationPortWithReservationResult(true),
            directionsPort = noopDirectionsPort(),
            controlPort = noopControlPort(),
            pendingDispatchStore = PendingDispatchStore(),
            transactionTemplate = TransactionTemplate(object : PlatformTransactionManager {
                override fun getTransaction(definition: org.springframework.transaction.TransactionDefinition?) =
                    org.springframework.transaction.support.SimpleTransactionStatus()
                override fun commit(status: org.springframework.transaction.TransactionStatus) = Unit
                override fun rollback(status: org.springframework.transaction.TransactionStatus) = Unit
            }),
            clock = Clock.fixed(Instant.parse("2026-04-27T01:00:00Z"), ZoneOffset.UTC),
        )

        val exception = assertFailsWith<IllegalArgumentException> {
            service.confirm(ConfirmDispatchCommand(requestId = 1L, userId = 2L))
        }

        assertEquals("PRE배차 요청을 찾을 수 없습니다.", exception.message)
    }

    @Test
    fun `이미 처리된 PRE배차 요청이면 예외를 던진다`() {
        val preRequest = DispatchRequest.pending(
            userId = 2L,
            origin = GeoPoint(longitude = 127.1, latitude = 37.4),
            destination = GeoPoint(longitude = 127.2, latitude = 37.5),
            fare = 12_100,
            routePath = emptyList(),
            estimatedTime = 46,
            distanceKm = 13.8,
            now = OffsetDateTime.ofInstant(Instant.parse("2026-04-27T00:00:00Z"), ZoneOffset.UTC),
        ).copy(id = 1L, status = DispatchRequestStatus.MATCHED)
        val service = ConfirmDispatchService(
            dispatchRequestRepository = object : DispatchRequestRepository {
                override fun save(request: DispatchRequest): Long = requireNotNull(request.id)
                override fun findById(requestId: Long): DispatchRequest? = preRequest
            },
            dispatchRepository = object : DispatchRepository {
                override fun save(dispatch: Dispatch): Long = error("사용하지 않습니다.")
                override fun findById(dispatchId: Long): Dispatch? = null
                override fun update(dispatch: Dispatch) = Unit
                override fun findActiveByCarId(carId: Long): Dispatch? = null
            },
            dispatchableCarProjection = dispatchableCarProjectionWith(101L),
            vehicleReservationPort = vehicleReservationPortWithReservationResult(true),
            directionsPort = noopDirectionsPort(),
            controlPort = noopControlPort(),
            pendingDispatchStore = PendingDispatchStore(),
            transactionTemplate = TransactionTemplate(object : PlatformTransactionManager {
                override fun getTransaction(definition: org.springframework.transaction.TransactionDefinition?) =
                    org.springframework.transaction.support.SimpleTransactionStatus()
                override fun commit(status: org.springframework.transaction.TransactionStatus) = Unit
                override fun rollback(status: org.springframework.transaction.TransactionStatus) = Unit
            }),
            clock = Clock.fixed(Instant.parse("2026-04-27T00:05:00Z"), ZoneOffset.UTC),
        )

        val exception = assertFailsWith<IllegalArgumentException> {
            service.confirm(ConfirmDispatchCommand(requestId = 1L, userId = 2L))
        }

        assertEquals("이미 처리된 PRE배차 요청입니다.", exception.message)
    }

    @Test
    fun `만료된 PRE배차 요청이면 예외를 던진다`() {
        val preRequest = DispatchRequest.pending(
            userId = 2L,
            origin = GeoPoint(longitude = 127.1, latitude = 37.4),
            destination = GeoPoint(longitude = 127.2, latitude = 37.5),
            fare = 12_100,
            routePath = emptyList(),
            estimatedTime = 46,
            distanceKm = 13.8,
            now = OffsetDateTime.ofInstant(Instant.parse("2026-04-27T00:00:00Z"), ZoneOffset.UTC),
        ).copy(id = 1L)
        val service = ConfirmDispatchService(
            dispatchRequestRepository = object : DispatchRequestRepository {
                override fun save(request: DispatchRequest): Long = requireNotNull(request.id)
                override fun findById(requestId: Long): DispatchRequest? = preRequest
            },
            dispatchRepository = object : DispatchRepository {
                override fun save(dispatch: Dispatch): Long = error("사용하지 않습니다.")
                override fun findById(dispatchId: Long): Dispatch? = null
                override fun update(dispatch: Dispatch) = Unit
                override fun findActiveByCarId(carId: Long): Dispatch? = null
            },
            dispatchableCarProjection = dispatchableCarProjectionWith(101L),
            vehicleReservationPort = vehicleReservationPortWithReservationResult(true),
            directionsPort = noopDirectionsPort(),
            controlPort = noopControlPort(),
            pendingDispatchStore = PendingDispatchStore(),
            transactionTemplate = TransactionTemplate(object : PlatformTransactionManager {
                override fun getTransaction(definition: org.springframework.transaction.TransactionDefinition?) =
                    org.springframework.transaction.support.SimpleTransactionStatus()
                override fun commit(status: org.springframework.transaction.TransactionStatus) = Unit
                override fun rollback(status: org.springframework.transaction.TransactionStatus) = Unit
            }),
            clock = Clock.fixed(Instant.parse("2026-04-27T00:10:01Z"), ZoneOffset.UTC),
        )

        val exception = assertFailsWith<IllegalArgumentException> {
            service.confirm(ConfirmDispatchCommand(requestId = 1L, userId = 2L))
        }

        assertEquals("만료된 PRE배차 요청입니다.", exception.message)
    }

    @Test
    fun `배차 가능한 차량이 없으면 예외를 던진다`() {
        val preRequest = DispatchRequest.pending(
            userId = 2L,
            origin = GeoPoint(longitude = 127.1, latitude = 37.4),
            destination = GeoPoint(longitude = 127.2, latitude = 37.5),
            fare = 12_100,
            routePath = emptyList(),
            estimatedTime = 46,
            distanceKm = 13.8,
            now = OffsetDateTime.ofInstant(Instant.parse("2026-04-27T00:00:00Z"), ZoneOffset.UTC),
        ).copy(id = 1L)
        val service = ConfirmDispatchService(
            dispatchRequestRepository = object : DispatchRequestRepository {
                override fun save(request: DispatchRequest): Long = error("사용하지 않습니다.")
                override fun findById(requestId: Long): DispatchRequest? = preRequest
            },
            dispatchRepository = object : DispatchRepository {
                override fun save(dispatch: Dispatch): Long = error("사용하지 않습니다.")
                override fun findById(dispatchId: Long): Dispatch? = null
                override fun update(dispatch: Dispatch) = Unit
                override fun findActiveByCarId(carId: Long): Dispatch? = null
            },
            dispatchableCarProjection = dispatchableCarProjectionWith(null),
            vehicleReservationPort = vehicleReservationPortWithReservationResult(true),
            directionsPort = noopDirectionsPort(),
            controlPort = noopControlPort(),
            pendingDispatchStore = PendingDispatchStore(),
            transactionTemplate = TransactionTemplate(object : PlatformTransactionManager {
                override fun getTransaction(definition: org.springframework.transaction.TransactionDefinition?) =
                    org.springframework.transaction.support.SimpleTransactionStatus()
                override fun commit(status: org.springframework.transaction.TransactionStatus) = Unit
                override fun rollback(status: org.springframework.transaction.TransactionStatus) = Unit
            }),
            clock = Clock.fixed(Instant.parse("2026-04-27T00:05:00Z"), ZoneOffset.UTC),
        )

        val exception = assertFailsWith<IllegalArgumentException> {
            service.confirm(ConfirmDispatchCommand(requestId = 1L, userId = 2L))
        }

        assertEquals("배차 가능한 차량을 찾을 수 없습니다.", exception.message)
    }

    @Test
    fun `첫 번째 차량 예약이 실패하고 두 번째 차량 예약이 성공하면 두 번째 차량으로 배차한다`() {
        val reservationAttempts = mutableListOf<Long>()
        val vehicleReservationPort = object : VehicleReservationPort {
            override fun reserve(carId: Long, ownerId: String): Boolean {
                reservationAttempts += carId
                return carId == 202L
            }

            override fun release(carId: Long, ownerId: String) = Unit
        }
        val preRequest = DispatchRequest.pending(
            userId = 2L,
            origin = GeoPoint(longitude = 127.1, latitude = 37.4),
            destination = GeoPoint(longitude = 127.2, latitude = 37.5),
            fare = 12_100,
            routePath = emptyList(),
            estimatedTime = 46,
            distanceKm = 13.8,
            now = OffsetDateTime.ofInstant(Instant.parse("2026-04-27T00:00:00Z"), ZoneOffset.UTC),
        ).copy(id = 1L)
        val service = ConfirmDispatchService(
            dispatchRequestRepository = object : DispatchRequestRepository {
                override fun save(request: DispatchRequest): Long = requireNotNull(request.id)
                override fun findById(requestId: Long): DispatchRequest? = preRequest
            },
            dispatchRepository = object : DispatchRepository {
                override fun save(dispatch: Dispatch): Long = 10L
                override fun findById(dispatchId: Long): Dispatch? = null
                override fun update(dispatch: Dispatch) = Unit
                override fun findActiveByCarId(carId: Long): Dispatch? = null
            },
            dispatchableCarProjection = object : DispatchableCarProjection {
                override fun saveIdleCarLocation(carId: Long, carNumber: String?, latitude: Double, longitude: Double) = Unit
                override fun removeCar(carId: Long) = Unit
                override fun findNearestIdleCars(latitude: Double, longitude: Double): List<DispatchableCarCandidate> = listOf(
                    DispatchableCarCandidate(carId = 101L, carNumber = "12가3456", distanceKm = 0.3, latitude = 37.4, longitude = 127.0),
                    DispatchableCarCandidate(carId = 202L, carNumber = "34나5678", distanceKm = 0.5, latitude = 37.41, longitude = 127.01),
                )
            },
            vehicleReservationPort = vehicleReservationPort,
            directionsPort = noopDirectionsPort(),
            controlPort = noopControlPort(),
            pendingDispatchStore = PendingDispatchStore(),
            transactionTemplate = TransactionTemplate(object : PlatformTransactionManager {
                override fun getTransaction(definition: org.springframework.transaction.TransactionDefinition?) =
                    org.springframework.transaction.support.SimpleTransactionStatus()
                override fun commit(status: org.springframework.transaction.TransactionStatus) = Unit
                override fun rollback(status: org.springframework.transaction.TransactionStatus) = Unit
            }),
            clock = Clock.fixed(Instant.parse("2026-04-27T00:05:00Z"), ZoneOffset.UTC),
        )

        val result = service.confirm(ConfirmDispatchCommand(requestId = 1L, userId = 2L))

        assertEquals(202L, result.carId)
        assertEquals(listOf(101L, 202L), reservationAttempts)
    }

    private fun dispatchableCarProjectionWith(
        carId: Long?,
        removedCars: MutableList<Long> = mutableListOf(),
    ): DispatchableCarProjection = object : DispatchableCarProjection {
        override fun saveIdleCarLocation(carId: Long, carNumber: String?, latitude: Double, longitude: Double) = Unit
        override fun removeCar(carId: Long) { removedCars += carId }
        override fun findNearestIdleCars(latitude: Double, longitude: Double): List<DispatchableCarCandidate> =
            carId?.let { listOf(DispatchableCarCandidate(carId = it, carNumber = "12가3456", distanceKm = 0.3, latitude = 37.4, longitude = 127.0)) }
                ?: emptyList()
    }

    private fun vehicleReservationPortWithReservationResult(reserved: Boolean): VehicleReservationPort = object : VehicleReservationPort {
        override fun reserve(carId: Long, ownerId: String): Boolean = reserved
        override fun release(carId: Long, ownerId: String) = Unit
    }

    private fun noopDirectionsPort(): DirectionsPort = object : DirectionsPort {
        override fun findRoute(origin: GeoPoint, destination: GeoPoint): RouteEstimate =
            RouteEstimate(fare = 0, routePath = emptyList(), durationSeconds = 0, distanceMeters = 0)
    }

    private fun noopControlPort(): ControlPort = object : ControlPort {
        override fun sendDispatchCommand(
            carId: Long, tripId: String, route: List<GeoPoint>,
            distanceMeters: Int, durationSeconds: Int, phase: String,
        ) = Unit
        override fun sendCancelDispatchCommand(carId: Long, tripId: String) = Unit
    }
}
