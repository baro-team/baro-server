package com.baro.dispatch.application.service

import com.baro.dispatch.domain.model.Dispatch
import com.baro.dispatch.domain.model.DispatchRequest
import com.baro.dispatch.domain.model.GeoPoint
import com.baro.dispatch.domain.repository.DispatchRepository
import com.baro.dispatch.domain.repository.DispatchRequestRepository
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ConfirmDispatchServiceTest {
    @Test
    fun `PRE배차 요청 ID로 실제 배차 요청을 생성한다`() {
        var savedDispatch: Dispatch? = null
        val preRequest = DispatchRequest.pending(
            userId = 2L,
            origin = GeoPoint(longitude = 127.1, latitude = 37.4),
            destination = GeoPoint(longitude = 127.2, latitude = 37.5),
            fare = 12_100,
            routePath = listOf(GeoPoint(longitude = 127.11, latitude = 37.41)),
            estimatedTime = 46,
            distanceKm = 13.8,
            now = OffsetDateTime.ofInstant(Instant.parse("2026-04-27T00:00:00Z"), ZoneOffset.UTC),
        )
        val service = ConfirmDispatchService(
            dispatchRequestRepository = object : DispatchRequestRepository {
                override fun save(request: DispatchRequest): Long = error("사용하지 않습니다.")
                override fun findById(requestId: Long): DispatchRequest? = preRequest.takeIf { requestId == 1L }
            },
            dispatchRepository = object : DispatchRepository {
                override fun save(dispatch: Dispatch): Long {
                    savedDispatch = dispatch
                    return 10L
                }
            },
            clock = Clock.fixed(Instant.parse("2026-04-27T01:00:00Z"), ZoneOffset.UTC),
        )

        val result = service.confirm(ConfirmDispatchCommand(requestId = 1L, userId = 2L))

        assertEquals(10L, result.dispatchId)
        assertEquals(1L, result.requestId)
        assertEquals(2L, result.userId)
        assertEquals(0L, result.carId)
        assertEquals(0L, result.standId)
        assertEquals(0, result.estimatedPickupTime)
        assertEquals(46, result.estimatedRideTime)
        assertEquals(emptyList(), result.pickupRoutePath)
        assertEquals(preRequest.routePath, result.dropoffRoutePath)
        assertEquals(12_100, result.fare)
        assertEquals("REQUESTED", result.status)

        val dispatch = requireNotNull(savedDispatch)
        assertEquals(Instant.parse("2026-04-27T01:00:00Z"), dispatch.createdAt.toInstant())
        assertEquals(preRequest.routePath, dispatch.dropoffRoutePath)
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
            },
            clock = Clock.fixed(Instant.parse("2026-04-27T01:00:00Z"), ZoneOffset.UTC),
        )

        val exception = assertFailsWith<IllegalArgumentException> {
            service.confirm(ConfirmDispatchCommand(requestId = 1L, userId = 2L))
        }

        assertEquals("PRE배차 요청을 찾을 수 없습니다.", exception.message)
    }
}
