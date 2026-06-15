package com.baro.user.interfaces.rest

import com.baro.common.web.response.BaseResponse
import com.baro.user.interfaces.rest.dto.MeResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController {
    @GetMapping(AuthApiPaths.ME)
    @SecurityRequirement(name = "bearerAuth")
    fun me(
        @RequestHeader(AuthenticatedUserHeaders.USER_ID) authenticatedUserId: Long,
        @RequestHeader(AuthenticatedUserHeaders.EMAIL) authenticatedEmail: String,
    ): BaseResponse<MeResponse> =
        BaseResponse.success(
            MeResponse(
                userId = authenticatedUserId,
                email = authenticatedEmail,
            ),
        )
}
