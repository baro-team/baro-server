package com.baro.dispatch.infrastructure.security

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.web.SecurityFilterChain
import javax.crypto.spec.SecretKeySpec

@Configuration
class SecurityConfig {
    @Bean
    fun securityErrorResponseWriter(objectMapper: ObjectMapper) = SecurityErrorResponseWriter(objectMapper)

    @Bean
    @ConditionalOnMissingBean(JwtDecoder::class)
    fun jwtDecoder(jwtProperties: JwtProperties): JwtDecoder {
        require(jwtProperties.secret.toByteArray().size >= 32) { "JWT_SECRET은 32바이트 이상이어야 합니다." }

        val secretKey = SecretKeySpec(jwtProperties.secret.toByteArray(), "HmacSHA256")
        return NimbusJwtDecoder.withSecretKey(secretKey)
            .macAlgorithm(MacAlgorithm.HS256)
            .build()
            .apply {
                setJwtValidator(JwtValidators.createDefaultWithIssuer(jwtProperties.issuer))
            }
    }

    @Bean
    fun filterChain(
        http: HttpSecurity,
        errorResponseWriter: SecurityErrorResponseWriter,
    ): SecurityFilterChain =
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it.requestMatchers(
                    "/actuator/health",
                    "/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                ).permitAll().anyRequest().authenticated()
            }
            .exceptionHandling {
                it.authenticationEntryPoint(RestAuthenticationEntryPoint(errorResponseWriter))
                    .accessDeniedHandler(RestAccessDeniedHandler(errorResponseWriter))
            }
            .oauth2ResourceServer { it.jwt { } }
            .build()
}
