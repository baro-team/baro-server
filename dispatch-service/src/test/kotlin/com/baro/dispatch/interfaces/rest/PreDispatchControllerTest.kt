package com.baro.dispatch.interfaces.rest

import com.baro.common.web.error.CommonRestExceptionHandler
import com.baro.dispatch.application.port.out.DirectionsPort
import com.baro.dispatch.application.port.out.RouteEstimate
import com.baro.dispatch.application.service.PreDispatchService
import com.baro.dispatch.domain.model.DispatchRequest
import com.baro.dispatch.domain.model.GeoPoint
import com.baro.dispatch.domain.repository.DispatchRequestRepository
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

@WebMvcTest(PreDispatchController::class)
@Import(PreDispatchService::class, CommonRestExceptionHandler::class)
class PreDispatchControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var directionsPort: DirectionsPort

    @MockBean
    private lateinit var dispatchRequestRepository: DispatchRequestRepository

    @MockBean
    private lateinit var clock: Clock

    @Test
    fun `PRE배차 요청 시 예상 운행 정보를 반환한다`() {
        given(clock.instant()).willReturn(Instant.parse("2026-04-27T00:00:00Z"))
        given(clock.zone).willReturn(ZoneOffset.UTC)
        given(dispatchRequestRepository.save(`임의의 배차 요청`()))
            .willReturn(1L)
        given(directionsPort.findRoute(`임의의 좌표`(), `임의의 좌표`()))
            .willReturn(
                RouteEstimate(
                    fare = 12100,
                    routePath = listOf(
                        GeoPoint(longitude = 12.123, latitude = 11.131),
                        GeoPoint(longitude = 11.121, latitude = 12.131),
                    ),
                    durationSeconds = 2_701,
                    distanceMeters = 13_840,
                ),
            )

        mockMvc.post(DispatchApiPaths.PRE_DISPATCH_FULL) {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "user_id": 2,
                  "origin": {"lat": 37.402464820205246, "lon": 127.10764191124568},
                  "destination": {"lat": 37.39419693653072, "lon": 127.11056336672839}
                }
            """.trimIndent()
        }.andExpect {
            status { isOk() }
            jsonPath("$.request_id") { value(1) }
            jsonPath("$.fare") { value(12100) }
            jsonPath("$.route_path[0][0]") { value(12.123) }
            jsonPath("$.estimated_time") { value(46) }
            jsonPath("$.distance_km") { value(13.8) }
        }
    }

    private fun `임의의 좌표`(): GeoPoint =
        any(GeoPoint::class.java) ?: GeoPoint(longitude = 0.0, latitude = 0.0)

    private fun `임의의 배차 요청`(): DispatchRequest =
        any(DispatchRequest::class.java)
            ?: DispatchRequest.pending(
                userId = 1L,
                origin = GeoPoint(longitude = 0.0, latitude = 0.0),
                destination = GeoPoint(longitude = 0.0, latitude = 0.0),
                now = OffsetDateTime.ofInstant(Instant.parse("2026-04-27T00:00:00Z"), ZoneOffset.UTC),
            )
}
