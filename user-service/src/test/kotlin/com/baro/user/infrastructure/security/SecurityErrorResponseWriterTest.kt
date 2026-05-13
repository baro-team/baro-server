package com.baro.user.infrastructure.security

import com.baro.common.web.response.ErrorCode
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletResponse

class SecurityErrorResponseWriterTest {
    private val writer = SecurityErrorResponseWriter(ObjectMapper())

    @Test
    fun `401 응답은 UNAUTHORIZED 에러 코드를 사용한다`() {
        val response = MockHttpServletResponse()

        writer.write(response, 401, "인증이 필요합니다.")

        assertEquals(401, response.status)
        assertEquals(ErrorCode.UNAUTHORIZED.name, response.contentAsString.let { Regex("\"code\":\"([^\"]+)\"").find(it)!!.groupValues[1] })
    }

    @Test
    fun `403 응답은 FORBIDDEN 에러 코드를 사용한다`() {
        val response = MockHttpServletResponse()

        writer.write(response, 403, "접근 권한이 없습니다.")

        assertEquals(403, response.status)
        assertEquals(ErrorCode.FORBIDDEN.name, response.contentAsString.let { Regex("\"code\":\"([^\"]+)\"").find(it)!!.groupValues[1] })
    }
}
