package com.baro.user.infrastructure.persistence

import com.baro.user.domain.model.UserRole
import com.baro.user.domain.model.UserStatus
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity @Table(name="users")
class UserEntity(@Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null, @Column(nullable=false, unique=true) var email: String = "", @Column(name="password_hash", nullable=false) var passwordHash: String = "", @Enumerated(EnumType.STRING) @Column(nullable=false) var role: UserRole = UserRole.USER, @Enumerated(EnumType.STRING) @Column(nullable=false) var status: UserStatus = UserStatus.ACTIVE, @Column(nullable=false) var createdAt: LocalDateTime = LocalDateTime.now(), @Column(nullable=false) var updatedAt: LocalDateTime = LocalDateTime.now())

@Entity @Table(name="refresh_tokens")
class RefreshTokenEntity(@Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null, @Column(name="user_id", nullable=false) var userId: Long = 0, @Column(name="token_hash", nullable=false, unique=true) var tokenHash: String = "", @Column(nullable=false) var expiresAt: LocalDateTime = LocalDateTime.now(), var revokedAt: LocalDateTime? = null, @Column(nullable=false) var createdAt: LocalDateTime = LocalDateTime.now())
