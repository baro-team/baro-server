package com.baro.user.infrastructure.persistence

import com.baro.user.domain.model.*
import com.baro.user.domain.repository.*
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class UserRepositoryAdapter(private val repo: UserJpaRepository) : UserRepository {
    override fun existsByEmail(email: String) = repo.existsByEmail(email)
    override fun findByEmail(email: String) = repo.findByEmail(email)?.toDomain()
    override fun findById(id: Long) = repo.findById(id).orElse(null)?.toDomain()
    override fun save(user: User) = repo.save(user.toEntity()).toDomain()
}

@Repository
class RefreshTokenRepositoryAdapter(private val repo: RefreshTokenJpaRepository) : RefreshTokenRepository {
    override fun save(token: RefreshToken) = repo.save(token.toEntity()).toDomain()
    override fun findValidByTokenHash(tokenHash: String, now: LocalDateTime) = repo.findByTokenHashAndRevokedAtIsNullAndExpiresAtAfter(tokenHash, now)?.toDomain()
    override fun findByTokenHash(tokenHash: String) = repo.findByTokenHash(tokenHash)?.toDomain()
    override fun revokeByTokenHash(tokenHash: String, revokedAt: LocalDateTime) { repo.findByTokenHash(tokenHash)?.apply { this.revokedAt = revokedAt }?.let(repo::save) }
}

private fun User.toEntity() = UserEntity(id, email, passwordHash, role, status, createdAt ?: LocalDateTime.now(), updatedAt ?: LocalDateTime.now())
private fun UserEntity.toDomain() = User(id, email, passwordHash, role, status, createdAt, updatedAt)
private fun RefreshToken.toEntity() = RefreshTokenEntity(id, userId, tokenHash, expiresAt, revokedAt, createdAt ?: LocalDateTime.now())
private fun RefreshTokenEntity.toDomain() = RefreshToken(id, userId, tokenHash, expiresAt, revokedAt, createdAt)
