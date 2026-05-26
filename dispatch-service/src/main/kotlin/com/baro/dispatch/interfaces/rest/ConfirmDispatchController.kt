package com.baro.dispatch.interfaces.rest

import com.baro.common.web.response.BaseResponse
import com.baro.dispatch.application.service.ConfirmDispatchService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(DispatchApiPaths.DISPATCH)
class ConfirmDispatchController(
    private val confirmDispatchService: ConfirmDispatchService,
) {
    @Operation(
        summary = "배차 요청",
        description = "PRE배차 요청 ID를 기반으로 실제 배차 요청을 생성합니다. 차량 조회/배정은 임시 값으로 처리합니다.",
    )
    @PostMapping
    fun confirm(
        @RequestBody request: ConfirmDispatchRequest,
        @AuthenticationPrincipal jwt: Jwt,
    ): BaseResponse<ConfirmDispatchResponse> {
        val authenticatedUserId = jwt.subject?.toLongOrNull()
        if (authenticatedUserId != request.userId) {
            throw AccessDeniedException("요청 사용자와 인증 사용자가 일치하지 않습니다.")
        }

        return BaseResponse.success(ConfirmDispatchResponse.from(confirmDispatchService.confirm(request.toCommand())))
    }
}
