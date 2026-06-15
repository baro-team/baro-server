package com.baro.user.infrastructure.security

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.JwtEncoder

@Configuration
@EnableConfigurationProperties(JwtProperties::class)
class SecurityConfig {
    @Bean fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean fun jwtTokenProvider(jwtProperties: JwtProperties) = JwtTokenProvider(jwtProperties)

    @Bean fun jwtEncoder(jwtTokenProvider: JwtTokenProvider): JwtEncoder = jwtTokenProvider.encoder
}
