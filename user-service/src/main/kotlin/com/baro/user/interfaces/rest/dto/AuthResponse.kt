package com.baro.user.interfaces.rest.dto

data class AuthResponse(
    val userId: Long,
    val email: String,
    val accessToken: String,
    val refreshToken: String,
)
