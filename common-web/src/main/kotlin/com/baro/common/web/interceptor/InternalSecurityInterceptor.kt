package com.baro.common.web.interceptor

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class InternalSecurityInterceptor(
    @Value("\${internal.api-key}")
    private val expectedApiKey: String
) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val path = request.requestURI
        
        // /internal 로 시작하는 요청에 대해서만 검증
        if (path.startsWith("/internal")) {
            val apiKey = request.getHeader("X-Internal-Api-Key")
            
            if (apiKey.isNullOrBlank() || apiKey != expectedApiKey) {
                response.sendError(HttpStatus.FORBIDDEN.value(), "Forbidden: Invalid or missing API Key")
                return false
            }
        }
        
        return true
    }
}
