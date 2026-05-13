package com.baro.user.application.service

import com.baro.user.domain.exception.DuplicateEmailException
import com.baro.user.domain.exception.InvalidCredentialsException
import com.baro.user.domain.exception.InvalidRefreshTokenException
import com.baro.user.domain.exception.UserNotFoundException
import com.baro.user.domain.model.RefreshToken
import com.baro.user.domain.model.User
import com.baro.user.domain.model.UserStatus
import com.baro.user.domain.repository.RefreshTokenRepository
import com.baro.user.domain.repository.UserRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneOffset

data class AuthResult(val userId: Long, val email: String, val accessToken: String, val refreshToken: String)

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenService: TokenService,
) {
    @Transactional
    fun signUp(email: String, password: String): AuthResult {
        if (userRepository.existsByEmail(email)) throw DuplicateEmailException()
        val saved = try {
            userRepository.save(User(email = email, passwordHash = passwordEncoder.encode(password)))
        } catch (e: DataIntegrityViolationException) {
            throw DuplicateEmailException()
        }
        val tokens = tokenService.createTokenPair(saved.id!!, saved.email)
        refreshTokenRepository.save(RefreshToken(userId = saved.id, tokenHash = tokenService.hashToken(tokens.refreshToken), expiresAt = tokenService.refreshExpiresAt()))
        return AuthResult(saved.id, saved.email, tokens.accessToken, tokens.refreshToken)
    }

    @Transactional
    fun login(email: String, password: String): AuthResult {
        val user = userRepository.findByEmail(email) ?: throw InvalidCredentialsException()
        if (user.status != UserStatus.ACTIVE) throw InvalidCredentialsException()
        if (!passwordEncoder.matches(password, user.passwordHash)) throw InvalidCredentialsException()
        val tokens = tokenService.createTokenPair(user.id!!, user.email)
        refreshTokenRepository.save(RefreshToken(userId = user.id, tokenHash = tokenService.hashToken(tokens.refreshToken), expiresAt = tokenService.refreshExpiresAt()))
        return AuthResult(user.id, user.email, tokens.accessToken, tokens.refreshToken)
    }

    @Transactional
    fun refresh(refreshToken: String): AuthResult {
        val hash = tokenService.hashToken(refreshToken)
        val token = refreshTokenRepository.findValidByTokenHash(hash, nowUtc()) ?: throw InvalidRefreshTokenException()
        val user = userRepository.findById(token.userId) ?: throw UserNotFoundException()
        if (user.status != UserStatus.ACTIVE) throw InvalidRefreshTokenException()
        val tokens = tokenService.createTokenPair(user.id!!, user.email)
        refreshTokenRepository.revokeByTokenHash(hash, nowUtc())
        refreshTokenRepository.save(RefreshToken(userId = user.id, tokenHash = tokenService.hashToken(tokens.refreshToken), expiresAt = tokenService.refreshExpiresAt()))
        return AuthResult(user.id, user.email, tokens.accessToken, tokens.refreshToken)
    }

    @Transactional
    fun logout(userId: Long, refreshToken: String) {
        val hash = tokenService.hashToken(refreshToken)
        val token = refreshTokenRepository.findByTokenHash(hash) ?: throw InvalidRefreshTokenException()
        if (token.userId != userId) throw InvalidRefreshTokenException()
        refreshTokenRepository.revokeByTokenHash(hash, nowUtc())
    }

    private fun nowUtc(): LocalDateTime = LocalDateTime.now(ZoneOffset.UTC)
}
