package com.baro.user.infrastructure.security

import jakarta.servlet.FilterChain
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import java.time.Instant

class JwtAuthenticationFilterTest {
    @Test fun `JWT가 있으면 인증 객체를 생성한다`() {
        val decoder = mock(JwtDecoder::class.java)
        val jwt = Jwt.withTokenValue("token").header("alg", "HS256").claim("email", "a@b.com").subject("1").issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build()
        whenever(decoder.decode("token")).thenReturn(jwt)
        val filter = JwtAuthenticationFilter(decoder)
        val request = MockHttpServletRequest().apply { addHeader("Authorization", "Bearer token") }
        filter.doFilter(request, MockHttpServletResponse(), FilterChain { _, _ -> })
        assertEquals("1", SecurityContextHolder.getContext().authentication?.principal?.let { (it as Jwt).subject })
        SecurityContextHolder.clearContext()
    }
}
