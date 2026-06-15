package com.baro.dispatch.interfaces.rest

import com.baro.common.web.config.CommonJacksonConfig
import com.baro.common.web.error.CommonRestExceptionHandler
import com.baro.dispatch.application.service.CancelDispatchCommand
import com.baro.dispatch.application.service.CancelDispatchResult
import com.baro.dispatch.application.service.CancelDispatchService
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@WebMvcTest(CancelDispatchController::class)
@Import(CommonJacksonConfig::class, CommonRestExceptionHandler::class)
class CancelDispatchControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var cancelDispatchService: CancelDispatchService

    @Test
    fun `인증된 배차 취소 요청 시 취소 결과를 반환한다`() {
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
            header(AuthenticatedUserHeaders.USER_ID, "2")
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
    fun `배차 취소 요청 시 인증 사용자 헤더가 없으면 잘못된 요청을 반환한다`() {
        mockMvc.post(DispatchApiPaths.CANCEL_DISPATCH_FULL, 10L)
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.error.code") { value("BAD_REQUEST") }
            }
    }
}
