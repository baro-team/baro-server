package com.baro.user.infrastructure.persistence

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import java.time.LocalDateTime

interface RefreshTokenJpaRepository : JpaRepository<RefreshTokenEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findByTokenHashAndRevokedAtIsNullAndExpiresAtAfter(tokenHash: String, now: LocalDateTime): RefreshTokenEntity?

    fun findByTokenHash(tokenHash: String): RefreshTokenEntity?
}
