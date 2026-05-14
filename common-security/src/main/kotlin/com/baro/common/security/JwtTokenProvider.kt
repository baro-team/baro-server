package com.baro.common.security

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.OctetSequenceKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.proc.SecurityContext
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class JwtTokenProvider(props: JwtProperties) {
    init {
        require(props.secret.toByteArray().size >= 32) { "JWT_SECRET은 32바이트 이상이어야 합니다." }
    }

    private val secretKey: SecretKey = SecretKeySpec(props.secret.toByteArray(), "HmacSHA256")

    val encoder: JwtEncoder = NimbusJwtEncoder(
        ImmutableJWKSet<SecurityContext>(
            JWKSet(
                OctetSequenceKey.Builder(secretKey.encoded)
                    .algorithm(JWSAlgorithm.HS256)
                    .build(),
            ),
        ),
    )

    val decoder: JwtDecoder = NimbusJwtDecoder.withSecretKey(secretKey)
        .macAlgorithm(MacAlgorithm.HS256)
        .build()
        .apply {
            setJwtValidator(JwtValidators.createDefaultWithIssuer(props.issuer))
        }
}
