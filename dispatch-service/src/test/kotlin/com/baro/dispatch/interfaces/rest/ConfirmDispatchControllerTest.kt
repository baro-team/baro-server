package com.baro.dispatch.interfaces.rest

import com.baro.common.web.config.CommonJacksonConfig
import com.baro.common.web.error.CommonRestExceptionHandler
import com.baro.dispatch.application.service.ConfirmDispatchResult
import com.baro.dispatch.application.service.ConfirmDispatchService
import com.baro.dispatch.application.service.ConfirmDispatchCommand
import com.baro.dispatch.domain.model.GeoPoint
import com.baro.dispatch.infrastructure.security.SecurityConfig
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.time.Instant

@WebMvcTest(ConfirmDispatchController::class)
@Import(CommonJacksonConfig::class, CommonRestExceptionHandler::class, DispatchRestExceptionHandler::class, SecurityConfig::class)
class ConfirmDispatchControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var confirmDispatchService: ConfirmDispatchService

    @MockBean
    private lateinit var jwtDecoder: JwtDecoder

    @Test
    fun `인증된 배차 요청 시 배차 정보를 반환한다`() {
        given(jwtDecoder.decode("access-token")).willReturn(`인증 토큰`())
        given(confirmDispatchService.confirm(ConfirmDispatchCommand(requestId = 1L, userId = 2L))).willReturn(
            ConfirmDispatchResult(
                dispatchId = 10L,
                requestId = 1L,
                userId = 2L,
                carId = 0L,
                carNumber = "12가3456",
                standId = 0L,
                estimatedPickupTime = 0,
                estimatedRideTime = 46,
                pickupRoutePath = emptyList(),
                dropoffRoutePath = listOf(GeoPoint(longitude = 127.1, latitude = 37.4)),
                fare = 12_100,
                status = "REQUESTED",
            ),
        )

        mockMvc.post(DispatchApiPaths.CONFIRM_DISPATCH_FULL) {
            contentType = MediaType.APPLICATION_JSON
            header("Authorization", "Bearer access-token")
            content = """{"request_id":1}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.dispatch_id") { value(10) }
            jsonPath("$.data.request_id") { value(1) }
            jsonPath("$.data.car_number") { value("12가3456") }
            jsonPath("$.data.user_id") { doesNotExist() }
            jsonPath("$.data.stand_id") { doesNotExist() }
            jsonPath("$.data.dispatch_status") { value("REQUESTED") }
        }
    }

    @Test
    fun `배차 요청 시 엑세스 토큰이 없으면 인증 오류를 반환한다`() {
        mockMvc.post(DispatchApiPaths.CONFIRM_DISPATCH_FULL) {
            contentType = MediaType.APPLICATION_JSON
            content = """{"request_id":1}"""
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.error.code") { value("UNAUTHORIZED") }
        }
    }

    private fun `인증 토큰`(): Jwt =
        Jwt.withTokenValue("access-token")
            .header("alg", "HS256")
            .subject("2")
            .claim("email", "user@example.com")
            .issuedAt(Instant.parse("2026-04-27T00:00:00Z"))
            .expiresAt(Instant.parse("2026-04-27T00:15:00Z"))
            .build()
}
