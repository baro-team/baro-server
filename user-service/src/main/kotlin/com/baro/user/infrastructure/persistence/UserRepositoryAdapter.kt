package com.baro.user.infrastructure.persistence

import com.baro.user.domain.model.User
import com.baro.user.domain.repository.UserRepository
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryAdapter(
    private val repo: UserJpaRepository,
) : UserRepository {
    override fun existsByEmail(email: String): Boolean = repo.existsByEmail(email)

    override fun findByEmail(email: String): User? = repo.findByEmail(email)?.toDomain()

    override fun findById(id: Long): User? = repo.findById(id).orElse(null)?.toDomain()

    override fun save(user: User): User = repo.save(UserEntity.from(user)).toDomain()
}
