package com.baro.gateway.security

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.OctetSequenceKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.proc.SecurityContext
import org.junit.jupiter.api.Test
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.atomic.AtomicBoolean
import javax.crypto.spec.SecretKeySpec
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue

class JwtAuthenticationGatewayFilterFactoryTest {
    private val secret = "12345678901234567890123456789012"
    private val filterFactory = JwtAuthenticationGatewayFilterFactory(GatewayJwtProperties(secret))

    @Test
    fun `인증 헤더가 없으면 401을 반환하고 라우팅하지 않는다`() {
        val routed = AtomicBoolean(false)
        val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/dispatch/pre"))

        StepVerifier.create(filterFactory.apply(JwtAuthenticationGatewayFilterFactory.Config()).filter(exchange, chain(routed)))
            .verifyComplete()

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.response.statusCode)
        assertFalse(routed.get())
    }

    @Test
    fun `유효하지 않은 토큰이면 401을 반환하고 라우팅하지 않는다`() {
        val routed = AtomicBoolean(false)
        val exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/dispatch/pre")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token"),
        )

        StepVerifier.create(filterFactory.apply(JwtAuthenticationGatewayFilterFactory.Config()).filter(exchange, chain(routed)))
            .verifyComplete()

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.response.statusCode)
        assertFalse(routed.get())
    }

    @Test
    fun `유효한 토큰이면 인증 헤더를 주입하고 라우팅한다`() {
        val routed = AtomicBoolean(false)
        val forwardedRequest = AtomicReference<org.springframework.http.server.reactive.ServerHttpRequest>()
        val exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/dispatch/pre")
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${token()}" )
                .header(GatewayAuthenticationHeaders.USER_ID, "spoofed")
                .header(GatewayAuthenticationHeaders.EMAIL, "spoofed@example.com"),
        )

        StepVerifier.create(filterFactory.apply(JwtAuthenticationGatewayFilterFactory.Config()).filter(exchange, chain(routed, forwardedRequest)))
            .verifyComplete()

        assertTrue(routed.get())
        assertEquals("7", forwardedRequest.get().headers.getFirst(GatewayAuthenticationHeaders.USER_ID))
        assertEquals("user@example.com", forwardedRequest.get().headers.getFirst(GatewayAuthenticationHeaders.EMAIL))
        assertNull(forwardedRequest.get().headers.getFirst(HttpHeaders.AUTHORIZATION))
    }

    @Test
    fun `requiredRole이 설정되고 일치하는 role을 가진 토큰이면 통과한다`() {
        val routed = AtomicBoolean(false)
        val exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/dispatch/vehicles/1/active")
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${token(role = "ADMIN")}"),
        )
        val config = JwtAuthenticationGatewayFilterFactory.Config().apply { requiredRole = "ADMIN" }

        StepVerifier.create(filterFactory.apply(config).filter(exchange, chain(routed)))
            .verifyComplete()

        assertTrue(routed.get())
    }

    @Test
    fun `requiredRole이 설정되고 role이 소문자여도 대소문자 무관하게 통과한다`() {
        val routed = AtomicBoolean(false)
        val exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/dispatch/vehicles/1/active")
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${token(role = "admin")}"),
        )
        val config = JwtAuthenticationGatewayFilterFactory.Config().apply { requiredRole = "ADMIN" }

        StepVerifier.create(filterFactory.apply(config).filter(exchange, chain(routed)))
            .verifyComplete()

        assertTrue(routed.get())
    }

    @Test
    fun `requiredRole이 설정되고 다른 role을 가진 토큰이면 403을 반환하고 라우팅하지 않는다`() {
        val routed = AtomicBoolean(false)
        val exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/dispatch/vehicles/1/active")
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${token(role = "USER")}"),
        )
        val config = JwtAuthenticationGatewayFilterFactory.Config().apply { requiredRole = "ADMIN" }

        StepVerifier.create(filterFactory.apply(config).filter(exchange, chain(routed)))
            .verifyComplete()

        assertEquals(HttpStatus.FORBIDDEN, exchange.response.statusCode)
        assertFalse(routed.get())
    }

    @Test
    fun `requiredRole이 설정되고 role claim이 없는 토큰이면 403을 반환하고 라우팅하지 않는다`() {
        val routed = AtomicBoolean(false)
        val exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/dispatch/vehicles/1/active")
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${token()}"),
        )
        val config = JwtAuthenticationGatewayFilterFactory.Config().apply { requiredRole = "ADMIN" }

        StepVerifier.create(filterFactory.apply(config).filter(exchange, chain(routed)))
            .verifyComplete()

        assertEquals(HttpStatus.FORBIDDEN, exchange.response.statusCode)
        assertFalse(routed.get())
    }

    @Test
    fun `email claim이 없으면 이메일 헤더를 주입하지 않는다`() {
        val routed = AtomicBoolean(false)
        val forwardedRequest = AtomicReference<org.springframework.http.server.reactive.ServerHttpRequest>()
        val exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/dispatch/pre")
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${token(includeEmail = false)}")
                .header(GatewayAuthenticationHeaders.EMAIL, "spoofed@example.com"),
        )

        StepVerifier.create(filterFactory.apply(JwtAuthenticationGatewayFilterFactory.Config()).filter(exchange, chain(routed, forwardedRequest)))
            .verifyComplete()

        assertTrue(routed.get())
        assertEquals("7", forwardedRequest.get().headers.getFirst(GatewayAuthenticationHeaders.USER_ID))
        assertNull(forwardedRequest.get().headers.getFirst(GatewayAuthenticationHeaders.EMAIL))
        assertNull(forwardedRequest.get().headers.getFirst(HttpHeaders.AUTHORIZATION))
    }

    private fun chain(
        routed: AtomicBoolean,
        forwardedRequest: AtomicReference<org.springframework.http.server.reactive.ServerHttpRequest>? = null,
    ): GatewayFilterChain = GatewayFilterChain { exchange ->
        routed.set(true)
        forwardedRequest?.set(exchange.request)
        Mono.empty()
    }

    private fun token(includeEmail: Boolean = true, role: String? = null): String {
        val secretKey = SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256")
        val encoder = NimbusJwtEncoder(
            ImmutableJWKSet<SecurityContext>(
                JWKSet(
                    OctetSequenceKey.Builder(secretKey.encoded)
                        .algorithm(JWSAlgorithm.HS256)
                        .build(),
                ),
            ),
        )
        return encoder.encode(
            JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                JwtClaimsSet.builder()
                    .subject("7")
                    .apply {
                        if (includeEmail) {
                            claim("email", "user@example.com")
                        }
                        if (role != null) {
                            claim("role", role)
                        }
                    }
                    .issuedAt(Instant.parse("2026-01-01T00:00:00Z"))
                    .expiresAt(Instant.parse("2099-01-01T00:00:00Z"))
                    .build(),
            ),
        ).tokenValue
    }
}
