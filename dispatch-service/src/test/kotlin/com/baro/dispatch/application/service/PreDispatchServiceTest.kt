package com.baro.dispatch.application.service

import com.baro.dispatch.application.port.out.DirectionsPort
import com.baro.dispatch.application.port.out.RouteEstimate
import com.baro.dispatch.domain.model.DispatchRequest
import com.baro.dispatch.domain.model.DispatchRequestStatus
import com.baro.dispatch.domain.model.GeoPoint
import com.baro.dispatch.domain.repository.DispatchRequestRepository
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals

class PreDispatchServiceTest {
    @Test
    fun `카카오 경로 정보를 PRE배차 응답으로 변환한다`() {
        var savedRequest: DispatchRequest? = null

        val service = PreDispatchService(
            directionsPort = object : DirectionsPort {
                override fun findRoute(origin: GeoPoint, destination: GeoPoint): RouteEstimate =
                    RouteEstimate(
                        fare = 12100,
                        routePath = listOf(
                            GeoPoint(longitude = 12.123, latitude = 11.131),
                            GeoPoint(longitude = 11.121, latitude = 12.131),
                            GeoPoint(longitude = 10.0, latitude = 20.0),
                        ),
                        durationSeconds = 2_701,
                        distanceMeters = 13_840,
                    )
            },
            dispatchRequestRepository = object : DispatchRequestRepository {
                override fun save(request: DispatchRequest): Long {
                    savedRequest = request
                    return 1L
                }
            },
            clock = Clock.fixed(Instant.parse("2026-04-27T00:00:00Z"), ZoneOffset.UTC),
        )

        val response = service.estimate(
            PreDispatchCommand(
                userId = 2L,
                origin = GeoPoint(longitude = 127.1, latitude = 37.4),
                destination = GeoPoint(longitude = 127.2, latitude = 37.5),
            ),
        )

        assertEquals(1L, response.requestId)
        assertEquals(12100, response.fare)
        assertEquals(
            listOf(
                GeoPoint(longitude = 12.123, latitude = 11.131),
                GeoPoint(longitude = 11.121, latitude = 12.131),
                GeoPoint(longitude = 10.0, latitude = 20.0),
            ),
            response.routePath,
        )
        assertEquals(46, response.estimatedTime)
        assertEquals(13.8, response.distanceKm)

        val request = requireNotNull(savedRequest)
        assertEquals(2L, request.userId)
        assertEquals(GeoPoint(longitude = 127.1, latitude = 37.4), request.origin)
        assertEquals(GeoPoint(longitude = 127.2, latitude = 37.5), request.destination)
        assertEquals(DispatchRequestStatus.PENDING, request.status)
        assertEquals(Instant.parse("2026-04-27T00:00:00Z"), request.requestedAt.toInstant())
        assertEquals(Instant.parse("2026-04-27T00:00:00Z"), request.updatedAt.toInstant())
        assertEquals(12100, request.fare)
        assertEquals(
            listOf(
                GeoPoint(longitude = 12.123, latitude = 11.131),
                GeoPoint(longitude = 11.121, latitude = 12.131),
                GeoPoint(longitude = 10.0, latitude = 20.0),
            ),
            request.routePath,
        )
        assertEquals(46, request.estimatedTime)
        assertEquals(13.8, request.distanceKm)
    }
}
