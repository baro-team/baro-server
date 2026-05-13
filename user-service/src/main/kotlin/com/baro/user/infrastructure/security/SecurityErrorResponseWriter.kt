package com.baro.user.infrastructure.security

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
        response.status = status
        response.contentType = "application/json"
        response.characterEncoding = "UTF-8"
        objectMapper.writeValue(response.writer, BaseResponse.error(ErrorCode.BAD_REQUEST, message))
    }
}
