package com.baro.user.infrastructure.persistence

import com.baro.user.domain.model.RefreshToken
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "refresh_tokens")
class RefreshTokenEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "user_id", nullable = false)
    var userId: Long = 0,

    @Column(name = "token_hash", nullable = false, unique = true)
    var tokenHash: String = "",

    @Column(nullable = false)
    var expiresAt: LocalDateTime = LocalDateTime.now(),

    var revokedAt: LocalDateTime? = null,

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),
) {
    fun toDomain(): RefreshToken =
        RefreshToken(
            id = id,
            userId = userId,
            tokenHash = tokenHash,
            expiresAt = expiresAt,
            revokedAt = revokedAt,
            createdAt = createdAt,
        )

    companion object {
        fun from(refreshToken: RefreshToken): RefreshTokenEntity =
            RefreshTokenEntity(
                id = refreshToken.id,
                userId = refreshToken.userId,
                tokenHash = refreshToken.tokenHash,
                expiresAt = refreshToken.expiresAt,
                revokedAt = refreshToken.revokedAt,
                createdAt = refreshToken.createdAt ?: LocalDateTime.now(),
            )
    }
}
