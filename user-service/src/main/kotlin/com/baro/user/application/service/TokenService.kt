package com.baro.user.application.service

import com.baro.user.infrastructure.security.JwtProperties
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

@Service
class TokenService(
    private val jwtEncoder: JwtEncoder,
    private val jwtProperties: JwtProperties,
) {
    fun createTokenPair(userId: Long, email: String): TokenPair {
        val now = Instant.now()
        val access = jwtEncoder.encode(
            JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                JwtClaimsSet.builder()
                    .issuer(jwtProperties.issuer)
                    .subject(userId.toString())
                    .claim("email", email)
                    .issuedAt(now)
                    .expiresAt(now.plusSeconds(jwtProperties.accessTokenExpirationSeconds))
                    .id(UUID.randomUUID().toString())
                    .build(),
            ),
        ).tokenValue
        val refresh = UUID.randomUUID().toString()
        return TokenPair(access, refresh)
    }

    fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(token.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun refreshExpiresAt(): LocalDateTime = LocalDateTime.now(ZoneOffset.UTC).plusSeconds(jwtProperties.refreshTokenExpirationSeconds)
}
