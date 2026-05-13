package com.baro.user.infrastructure.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthenticationFilter(
    private val decoder: org.springframework.security.oauth2.jwt.JwtDecoder,
    private val errorResponseWriter: SecurityErrorResponseWriter,
) : OncePerRequestFilter() {
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val header = request.getHeader("Authorization")
        if (header?.startsWith("Bearer ") == true) {
            try {
                val jwt = decoder.decode(header.removePrefix("Bearer ").trim())
                SecurityContextHolder.getContext().authentication = JwtAuthenticationToken(jwt)
            } catch (e: JwtException) {
                SecurityContextHolder.clearContext()
                errorResponseWriter.write(response, HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 인증 토큰입니다.")
                return
            }
        }
        filterChain.doFilter(request, response)
    }
}
