package com.baro.dispatch.interfaces.rest

import com.baro.common.web.response.BaseResponse
import com.baro.dispatch.application.service.CancelDispatchCommand
import com.baro.dispatch.application.service.CancelDispatchService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping(DispatchApiPaths.DISPATCH)
class CancelDispatchController(
    private val cancelDispatchService: CancelDispatchService,
) {
    @Operation(
        summary = "배차 취소",
        description = "차량이 출발지로 이동 중인 배차 요청을 취소합니다.",
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(DispatchApiPaths.CANCEL_DISPATCH)
    fun cancel(
        @PathVariable dispatchId: Long,
        @RequestHeader(AuthenticatedUserHeaders.USER_ID) authenticatedUserIdHeader: String,
    ): BaseResponse<CancelDispatchResponse> {
        val authenticatedUserId = authenticatedUserIdHeader.toLongOrNull()
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 사용자 정보가 올바르지 않습니다.")

        return BaseResponse.success(
            CancelDispatchResponse.from(
                cancelDispatchService.cancel(
                    CancelDispatchCommand(
                        dispatchId = dispatchId,
                        userId = authenticatedUserId,
                    ),
                ),
            ),
        )
    }
}
