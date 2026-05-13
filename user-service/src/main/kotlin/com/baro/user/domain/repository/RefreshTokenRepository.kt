package com.baro.user.domain.repository

import com.baro.user.domain.model.RefreshToken
import java.time.LocalDateTime

interface RefreshTokenRepository {
    fun save(token: RefreshToken): RefreshToken
    fun findValidByTokenHash(tokenHash: String, now: LocalDateTime): RefreshToken?
    fun findByTokenHash(tokenHash: String): RefreshToken?
    fun revokeByTokenHash(tokenHash: String, revokedAt: LocalDateTime)
}
