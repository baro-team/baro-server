package com.baro.user.application.service

data class TokenPair(
    val accessToken: String,
    val refreshToken: String,
)
