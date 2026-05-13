package com.baro.user.domain.repository

import com.baro.user.domain.model.User

interface UserRepository {
    fun existsByEmail(email: String): Boolean
    fun findByEmail(email: String): User?
    fun findById(id: Long): User?
    fun save(user: User): User
}
