package com.baro.dispatch.infrastructure.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler

class RestAccessDeniedHandler(
    private val errorResponseWriter: SecurityErrorResponseWriter,
) : AccessDeniedHandler {
    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException,
    ) {
        errorResponseWriter.write(response, HttpServletResponse.SC_FORBIDDEN, "접근 권한이 없습니다.")
    }
}
