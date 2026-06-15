package com.baro.dispatch.interfaces.rest

import com.baro.common.web.config.CommonJacksonConfig
import com.baro.common.web.error.CommonRestExceptionHandler
import com.baro.dispatch.application.service.CancelDispatchCommand
import com.baro.dispatch.application.service.CancelDispatchResult
import com.baro.dispatch.application.service.CancelDispatchService
import com.baro.dispatch.infrastructure.security.SecurityConfig
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.time.Instant

@WebMvcTest(CancelDispatchController::class)
@Import(CommonJacksonConfig::class, CommonRestExceptionHandler::class, DispatchRestExceptionHandler::class, SecurityConfig::class)
class CancelDispatchControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var cancelDispatchService: CancelDispatchService

    @MockBean
    private lateinit var jwtDecoder: JwtDecoder

    @Test
    fun `인증된 배차 취소 요청 시 취소 결과를 반환한다`() {
        given(jwtDecoder.decode("access-token")).willReturn(`인증 토큰`())
        given(cancelDispatchService.cancel(CancelDispatchCommand(dispatchId = 10L, userId = 2L))).willReturn(
            CancelDispatchResult(
                dispatchId = 10L,
                requestId = 1L,
                carId = 101L,
                carNumber = "12가3456",
                status = "CANCELLED",
            ),
        )

        mockMvc.post(DispatchApiPaths.CANCEL_DISPATCH_FULL, 10L) {
            header("Authorization", "Bearer access-token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.dispatch_id") { value(10) }
            jsonPath("$.data.request_id") { value(1) }
            jsonPath("$.data.car_id") { value(101) }
            jsonPath("$.data.car_number") { value("12가3456") }
            jsonPath("$.data.dispatch_status") { value("CANCELLED") }
        }
    }

    @Test
    fun `배차 취소 요청 시 엑세스 토큰이 없으면 인증 오류를 반환한다`() {
        mockMvc.post(DispatchApiPaths.CANCEL_DISPATCH_FULL, 10L)
            .andExpect {
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
