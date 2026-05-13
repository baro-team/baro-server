package com.baro.user.interfaces.rest.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SignUpRequest(
    @field:Email(message = "이메일 형식이 올바르지 않습니다.")
    @field:NotBlank(message = "이메일은 필수입니다.")
    val email: String,

    @field:NotBlank(message = "비밀번호는 필수입니다.")
    @field:Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    val password: String,
)

data class LoginRequest(
    @field:Email(message = "이메일 형식이 올바르지 않습니다.")
    @field:NotBlank(message = "이메일은 필수입니다.")
    val email: String,

    @field:NotBlank(message = "비밀번호는 필수입니다.")
    val password: String,
)

data class RefreshTokenRequest(
    @field:NotBlank(message = "리프레시 토큰은 필수입니다.")
    val refreshToken: String,
)

data class AuthResponse(
    val userId: Long,
    val email: String,
    val accessToken: String,
    val refreshToken: String,
)

data class MeResponse(
    val userId: Long,
    val email: String,
)
