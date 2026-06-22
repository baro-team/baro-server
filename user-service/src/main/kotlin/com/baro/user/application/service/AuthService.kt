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
        val userId = checkNotNull(saved.id)
        val tokens = tokenService.createTokenPair(userId, saved.email, saved.role)
        refreshTokenRepository.save(RefreshToken(userId = userId, tokenHash = tokenService.hashToken(tokens.refreshToken), expiresAt = tokenService.refreshExpiresAt()))
        return AuthResult(userId, saved.email, tokens.accessToken, tokens.refreshToken)
    }

    @Transactional
    fun login(email: String, password: String): AuthResult {
        val user = userRepository.findByEmail(email) ?: throw InvalidCredentialsException()
        if (user.status != UserStatus.ACTIVE) throw InvalidCredentialsException()
        if (!passwordEncoder.matches(password, user.passwordHash)) throw InvalidCredentialsException()
        val userId = checkNotNull(user.id)
        val tokens = tokenService.createTokenPair(userId, user.email, user.role)
        refreshTokenRepository.save(RefreshToken(userId = userId, tokenHash = tokenService.hashToken(tokens.refreshToken), expiresAt = tokenService.refreshExpiresAt()))
        return AuthResult(userId, user.email, tokens.accessToken, tokens.refreshToken)
    }

    @Transactional
    fun refresh(refreshToken: String): AuthResult {
        val hash = tokenService.hashToken(refreshToken)
        val token = refreshTokenRepository.findValidByTokenHash(hash, nowUtc()) ?: throw InvalidRefreshTokenException()
        val user = userRepository.findById(token.userId) ?: throw UserNotFoundException()
        if (user.status != UserStatus.ACTIVE) throw InvalidRefreshTokenException()
        val userId = checkNotNull(user.id)
        val tokens = tokenService.createTokenPair(userId, user.email, user.role)
        refreshTokenRepository.revokeByTokenHash(hash, nowUtc())
        refreshTokenRepository.save(RefreshToken(userId = userId, tokenHash = tokenService.hashToken(tokens.refreshToken), expiresAt = tokenService.refreshExpiresAt()))
        return AuthResult(userId, user.email, tokens.accessToken, tokens.refreshToken)
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
