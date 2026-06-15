package com.baro.dispatch.interfaces.rest

import com.baro.common.web.config.CommonJacksonConfig
import com.baro.common.web.error.CommonRestExceptionHandler
import com.baro.dispatch.application.service.ConfirmDispatchResult
import com.baro.dispatch.application.service.ConfirmDispatchService
import com.baro.dispatch.application.service.ConfirmDispatchCommand
import com.baro.dispatch.domain.model.GeoPoint
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@WebMvcTest(ConfirmDispatchController::class)
@Import(
    CommonJacksonConfig::class,
    CommonRestExceptionHandler::class,
)
class ConfirmDispatchControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var confirmDispatchService: ConfirmDispatchService

    @Test
    fun `인증된 배차 요청 시 배차 정보를 반환한다`() {
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
            header(AuthenticatedUserHeaders.USER_ID, "2")
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
    fun `배차 요청 시 인증 사용자 헤더가 없으면 잘못된 요청을 반환한다`() {
        mockMvc.post(DispatchApiPaths.CONFIRM_DISPATCH_FULL) {
            contentType = MediaType.APPLICATION_JSON
            content = """{"request_id":1}"""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error.code") { value("BAD_REQUEST") }
        }
    }
}
