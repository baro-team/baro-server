package com.baro.dispatch.infrastructure.security

import com.baro.common.security.JwtProperties
import com.baro.common.security.JwtTokenProvider
import com.baro.common.security.RestAccessDeniedHandler
import com.baro.common.security.RestAuthenticationEntryPoint
import com.baro.common.security.SecurityErrorResponseWriter
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableConfigurationProperties(JwtProperties::class)
class SecurityConfig {
    @Bean
    fun securityErrorResponseWriter(objectMapper: ObjectMapper) = SecurityErrorResponseWriter(objectMapper)

    @Bean
    fun jwtDecoder(jwtProperties: JwtProperties) = JwtTokenProvider(jwtProperties).decoder

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
                    "/dispatch/api-docs/**",
                    "/dispatch/api-docs",
                    "/dispatch/swagger-ui/**",
                    "/dispatch/swagger-ui.html",
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
