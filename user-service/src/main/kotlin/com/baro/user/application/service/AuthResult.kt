package com.baro.user.application.service

data class AuthResult(
    val userId: Long,
    val email: String,
    val accessToken: String,
    val refreshToken: String,
)
