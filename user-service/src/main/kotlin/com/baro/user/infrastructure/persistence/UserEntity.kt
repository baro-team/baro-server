package com.baro.user.infrastructure.persistence

import com.baro.user.domain.model.User
import com.baro.user.domain.model.UserRole
import com.baro.user.domain.model.UserStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "users")
class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, unique = true)
    var email: String = "",

    @Column(name = "password_hash", nullable = false)
    var passwordHash: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: UserRole = UserRole.USER,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: UserStatus = UserStatus.ACTIVE,

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    fun toDomain(): User =
        User(
            id = id,
            email = email,
            passwordHash = passwordHash,
            role = role,
            status = status,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    companion object {
        fun from(user: User): UserEntity =
            UserEntity(
                id = user.id,
                email = user.email,
                passwordHash = user.passwordHash,
                role = user.role,
                status = user.status,
                createdAt = user.createdAt ?: LocalDateTime.now(),
                updatedAt = user.updatedAt ?: LocalDateTime.now(),
            )
    }
}
