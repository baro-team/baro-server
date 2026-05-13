package com.baro.user.infrastructure.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.access.AccessDeniedHandler

class RestAuthenticationEntryPoint : AuthenticationEntryPoint {
    override fun commence(request: HttpServletRequest, response: HttpServletResponse, authException: AuthenticationException) {
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = "application/json"
        response.characterEncoding = "UTF-8"
        response.writer.write("{\"success\":false,\"data\":null,\"error\":{\"code\":\"BAD_REQUEST\",\"message\":\"인증이 필요합니다.\"}}")
    }
}
class RestAccessDeniedHandler : AccessDeniedHandler {
    override fun handle(request: HttpServletRequest, response: HttpServletResponse, accessDeniedException: AccessDeniedException) {
        response.status = HttpServletResponse.SC_FORBIDDEN
        response.contentType = "application/json"
        response.characterEncoding = "UTF-8"
        response.writer.write("{\"success\":false,\"data\":null,\"error\":{\"code\":\"BAD_REQUEST\",\"message\":\"접근 권한이 없습니다.\"}}")
    }
}
