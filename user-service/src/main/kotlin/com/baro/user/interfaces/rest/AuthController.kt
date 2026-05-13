package com.baro.user.interfaces.rest

import com.baro.common.web.response.BaseResponse
import com.baro.user.application.service.AuthResult
import com.baro.user.application.service.AuthService
import com.baro.user.interfaces.rest.dto.*
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@RestController
class AuthController(private val authService: AuthService) {
    @PostMapping(AuthApiPaths.SIGN_UP)
    fun signUp(@Valid @RequestBody req: SignUpRequest): BaseResponse<AuthResponse> =
        BaseResponse.success(authService.signUp(req.email, req.password).toResponse())

    @PostMapping(AuthApiPaths.LOGIN)
    fun login(@Valid @RequestBody req: LoginRequest): BaseResponse<AuthResponse> =
        BaseResponse.success(authService.login(req.email, req.password).toResponse())

    @PostMapping(AuthApiPaths.REFRESH)
    fun refresh(@Valid @RequestBody req: RefreshTokenRequest): BaseResponse<AuthResponse> =
        BaseResponse.success(authService.refresh(req.refreshToken).toResponse())

    @PostMapping(AuthApiPaths.LOGOUT)
    fun logout(
        @AuthenticationPrincipal jwt: Jwt,
        @Valid @RequestBody req: RefreshTokenRequest,
    ): BaseResponse<Unit> {
        authService.logout(jwt.subject.toLong(), req.refreshToken)
        return BaseResponse.success(Unit)
    }

    private fun AuthResult.toResponse(): AuthResponse =
        AuthResponse(
            userId = userId,
            email = email,
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
}
