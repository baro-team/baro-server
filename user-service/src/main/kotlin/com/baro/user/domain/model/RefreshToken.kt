package com.baro.user.domain.model

import java.time.LocalDateTime

data class RefreshToken(
    val id: Long? = null,
    val userId: Long,
    val tokenHash: String,
    val expiresAt: LocalDateTime,
    val revokedAt: LocalDateTime? = null,
    val createdAt: LocalDateTime? = null,
)
