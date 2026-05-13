package com.baro.user.interfaces.rest

import com.baro.common.web.response.BaseResponse
import com.baro.user.interfaces.rest.dto.MeResponse
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController {
    @GetMapping(AuthApiPaths.ME)
    fun me(@AuthenticationPrincipal jwt: Jwt): BaseResponse<MeResponse> =
        BaseResponse.success(
            MeResponse(
                userId = jwt.subject.toLong(),
                email = jwt.getClaimAsString("email"),
            ),
        )
}
