package com.baro.user.infrastructure.persistence

import com.baro.user.domain.model.RefreshToken
import com.baro.user.domain.repository.RefreshTokenRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class RefreshTokenRepositoryAdapter(
    private val repo: RefreshTokenJpaRepository,
) : RefreshTokenRepository {
    override fun save(token: RefreshToken): RefreshToken = repo.save(RefreshTokenEntity.from(token)).toDomain()

    override fun findValidByTokenHash(tokenHash: String, now: LocalDateTime): RefreshToken? =
        repo.findByTokenHashAndRevokedAtIsNullAndExpiresAtAfter(tokenHash, now)?.toDomain()

    override fun findByTokenHash(tokenHash: String): RefreshToken? = repo.findByTokenHash(tokenHash)?.toDomain()

    override fun revokeByTokenHash(tokenHash: String, revokedAt: LocalDateTime) {
        repo.findByTokenHash(tokenHash)
            ?.apply { this.revokedAt = revokedAt }
            ?.let(repo::save)
    }
}
