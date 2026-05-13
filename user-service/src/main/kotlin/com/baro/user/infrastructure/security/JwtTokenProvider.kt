package com.baro.user.infrastructure.security

import com.nimbusds.jose.jwk.OctetSequenceKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.proc.SecurityContext
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.*
import org.springframework.stereotype.Component
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@Component
class JwtTokenProvider(props: JwtProperties) {
    init {
        require(props.secret.toByteArray().size >= 32) { "JWT_SECRET은 32바이트 이상이어야 합니다." }
    }

    private val secretKey: SecretKey = SecretKeySpec(props.secret.toByteArray(), "HmacSHA256")
    val encoder: JwtEncoder = NimbusJwtEncoder(ImmutableJWKSet<SecurityContext>(com.nimbusds.jose.jwk.JWKSet(OctetSequenceKey.Builder(secretKey.encoded).build())))
    val decoder: JwtDecoder = NimbusJwtDecoder.withSecretKey(secretKey).build().apply {
        setJwtValidator(JwtValidators.createDefaultWithIssuer(props.issuer))
    }
}
