package com.baro.common.security

import com.baro.common.web.response.BaseResponse
import com.baro.common.web.response.ErrorCode
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletResponse

class SecurityErrorResponseWriter(
    private val objectMapper: ObjectMapper,
) {
    fun write(
        response: HttpServletResponse,
        status: Int,
        message: String,
    ) {
        val errorCode = when (status) {
            HttpServletResponse.SC_UNAUTHORIZED -> ErrorCode.UNAUTHORIZED
            HttpServletResponse.SC_FORBIDDEN -> ErrorCode.FORBIDDEN
            else -> ErrorCode.BAD_REQUEST
        }
        response.status = status
        response.contentType = "application/json"
        response.characterEncoding = "UTF-8"
        objectMapper.writeValue(response.writer, BaseResponse.error(errorCode, message))
    }
}
