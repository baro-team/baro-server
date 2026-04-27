package com.baro.dispatch.application.service

import com.baro.dispatch.application.port.out.DirectionsPort
import com.baro.dispatch.application.port.out.RouteEstimate
import com.baro.dispatch.domain.model.DispatchRequest
import com.baro.dispatch.domain.model.GeoPoint
import com.baro.dispatch.domain.repository.DispatchRequestRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class PreDispatchServiceTest {
    @Test
    fun `카카오 경로 정보를 PRE배차 응답으로 변환한다`() {
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
                override fun save(request: DispatchRequest): Long = 1L
            },
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
    }
}
