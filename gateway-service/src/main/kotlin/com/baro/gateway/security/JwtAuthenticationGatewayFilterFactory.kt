package com.baro.gateway.security

import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.stereotype.Component
import javax.crypto.spec.SecretKeySpec

@Component
class JwtAuthenticationGatewayFilterFactory(
    private val properties: GatewayJwtProperties,
) : AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilterFactory.Config>(Config::class.java) {
    private val log = LoggerFactory.getLogger(javaClass)
    private final val jwtDecoder: JwtDecoder

    init {
        val secretBytes = properties.secret.toByteArray(Charsets.UTF_8)
        require(secretBytes.size >= 32) { "JWT_SECRET은 32바이트 이상이어야 합니다." }
        val secretKey = SecretKeySpec(secretBytes, "HmacSHA256")
        jwtDecoder = NimbusJwtDecoder.withSecretKey(secretKey)
            .macAlgorithm(MacAlgorithm.HS256)
            .build()
    }

    class Config {
        var requiredRole: String? = null
    }

    override fun shortcutFieldOrder(): List<String> = listOf("requiredRole")

    override fun apply(config: Config): GatewayFilter = GatewayFilter { exchange, chain ->
        val token = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)
            ?.removePrefix("Bearer ")
            ?.takeIf { it.isNotBlank() }

        if (token == null) {
            log.warn("인증 헤더가 없는 요청입니다. path={}", exchange.request.path.value())
            exchange.response.statusCode = HttpStatus.UNAUTHORIZED
            return@GatewayFilter exchange.response.setComplete()
        }

        val jwt = try {
            jwtDecoder.decode(token)
        } catch (e: Exception) {
            log.warn("유효하지 않은 JWT입니다. path={}, error={}", exchange.request.path.value(), e.message)
            exchange.response.statusCode = HttpStatus.UNAUTHORIZED
            return@GatewayFilter exchange.response.setComplete()
        }

        val userId = jwt.subject?.takeIf { it.isNotBlank() }
        if (userId == null) {
            log.warn("JWT subject가 비어 있습니다. path={}", exchange.request.path.value())
            exchange.response.statusCode = HttpStatus.UNAUTHORIZED
            return@GatewayFilter exchange.response.setComplete()
        }

        val role = jwt.getClaimAsString("role")
        if (config.requiredRole != null && role != config.requiredRole) {
            log.warn("권한 없음. path={}, required={}, actual={}", exchange.request.path.value(), config.requiredRole, role)
            exchange.response.statusCode = HttpStatus.FORBIDDEN
            return@GatewayFilter exchange.response.setComplete()
        }

        val sanitizedRequest = exchange.request.mutate()
            .headers {
                it.remove(HttpHeaders.AUTHORIZATION)
                it.remove(GatewayAuthenticationHeaders.USER_ID)
                it.remove(GatewayAuthenticationHeaders.EMAIL)
                it.remove(GatewayAuthenticationHeaders.ROLE)
            }
            .header(GatewayAuthenticationHeaders.USER_ID, userId)
            .apply {
                jwt.getClaimAsString("email")?.takeIf(String::isNotBlank)?.let {
                    header(GatewayAuthenticationHeaders.EMAIL, it)
                }
                role?.takeIf(String::isNotBlank)?.let {
                    header(GatewayAuthenticationHeaders.ROLE, it)
                }
            }
            .build()

        chain.filter(exchange.mutate().request(sanitizedRequest).build())
    }
}
