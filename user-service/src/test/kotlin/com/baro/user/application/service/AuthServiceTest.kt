package com.baro.user.application.service

import com.baro.user.domain.model.User
import com.baro.user.domain.repository.RefreshTokenRepository
import com.baro.user.domain.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class AuthServiceTest {
    private val userRepository = mock<UserRepository>()
    private val refreshTokenRepository = mock<RefreshTokenRepository>()
    private val passwordEncoder = BCryptPasswordEncoder()
    private val tokenService = mock<TokenService>()
    private val service = AuthService(userRepository, refreshTokenRepository, passwordEncoder, tokenService)

    @Test fun `회원가입 시 이메일과 비밀번호로 사용자를 생성한다`() {
        `when`(userRepository.existsByEmail("a@b.com")).thenReturn(false)
        whenever(tokenService.createTokenPair(1L, "a@b.com")).thenReturn(TokenPair("access", "refresh"))
        whenever(tokenService.hashToken("refresh")).thenReturn("hash")
        whenever(tokenService.refreshExpiresAt()).thenReturn(java.time.LocalDateTime.now())
        whenever(userRepository.save(any())).thenReturn(User(1, "a@b.com", passwordEncoder.encode("pw")))
        val result = service.signUp("a@b.com", "pw")
        assertEquals(1L, result.userId)
        assertEquals("a@b.com", result.email)
    }
}
