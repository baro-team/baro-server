package com.baro.user.domain.model

import java.time.LocalDateTime

data class User(
    val id: Long? = null,
    val email: String,
    val passwordHash: String,
    val role: UserRole = UserRole.USER,
    val status: UserStatus = UserStatus.ACTIVE,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
)

enum class UserRole { USER, ADMIN }
enum class UserStatus { ACTIVE, INACTIVE }

data class RefreshToken(
    val id: Long? = null,
    val userId: Long,
    val tokenHash: String,
    val expiresAt: LocalDateTime,
    val revokedAt: LocalDateTime? = null,
    val createdAt: LocalDateTime? = null,
)
