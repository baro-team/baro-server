package com.baro.user.infrastructure.security

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
class SecurityConfig(private val jwtTokenProvider: JwtTokenProvider) {
    @Bean fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
    @Bean fun jwtEncoder() = jwtTokenProvider.encoder
    @Bean fun jwtDecoder() = jwtTokenProvider.decoder
    @Bean fun securityErrorResponseWriter(objectMapper: ObjectMapper) = SecurityErrorResponseWriter(objectMapper)
    @Bean fun jwtAuthenticationFilter(
        jwtDecoder: org.springframework.security.oauth2.jwt.JwtDecoder,
        errorResponseWriter: SecurityErrorResponseWriter,
    ) = JwtAuthenticationFilter(jwtDecoder, errorResponseWriter)

    @Bean fun filterChain(
        http: HttpSecurity,
        filter: JwtAuthenticationFilter,
        errorResponseWriter: SecurityErrorResponseWriter,
    ): SecurityFilterChain = http.csrf { it.disable() }.sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }.authorizeHttpRequests { it.requestMatchers("/actuator/health", "/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/auth/sign-up", "/auth/login", "/auth/token/refresh").permitAll().anyRequest().authenticated() }.exceptionHandling { it.authenticationEntryPoint(RestAuthenticationEntryPoint(errorResponseWriter)).accessDeniedHandler(RestAccessDeniedHandler(errorResponseWriter)) }.addFilterBefore(filter, UsernamePasswordAuthenticationFilter::class.java).build()
}
