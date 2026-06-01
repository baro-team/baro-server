package com.baro.common.web.error

import com.baro.common.core.exception.BadRequestException
import com.baro.common.core.exception.ExternalServiceException
import com.baro.common.web.response.ErrorCode
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class CommonRestExceptionHandlerTest {
    private val handler = CommonRestExceptionHandler()

    @Test
    fun `잘못된 요청 예외는 400 응답으로 변환한다`() {
        val response = handler.handleBadRequestException(BadRequestException("출발지는 필수입니다."))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertFalse(response.body?.success ?: true)
        assertEquals(ErrorCode.BAD_REQUEST.name, response.body?.error?.code)
        assertEquals("출발지는 필수입니다.", response.body?.error?.message)
    }

    @Test
    fun `외부 서비스 예외는 502 응답으로 변환한다`() {
        val response = handler.handleExternalServiceException(ExternalServiceException("카카오 API 호출에 실패했습니다."))

        assertEquals(HttpStatus.BAD_GATEWAY, response.statusCode)
        assertFalse(response.body?.success ?: true)
        assertEquals(ErrorCode.EXTERNAL_SERVICE_ERROR.name, response.body?.error?.code)
        assertEquals("카카오 API 호출에 실패했습니다.", response.body?.error?.message)
    }

    @Test
    fun `처리되지 않은 일반 예외는 500 BaseResponse 응답으로 변환한다`() {
        val response = handler.handleException(RuntimeException("회원 저장에 실패했습니다."))

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertFalse(response.body?.success ?: true)
        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR.name, response.body?.error?.code)
        assertEquals("회원 저장에 실패했습니다.", response.body?.error?.message)
    }

    @Test
    fun `처리되지 않은 일반 예외는 원인 예외 메시지도 함께 반환한다`() {
        val response = handler.handleException(RuntimeException("회원가입 처리 실패", IllegalStateException("users 테이블이 없습니다.")))

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertFalse(response.body?.success ?: true)
        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR.name, response.body?.error?.code)
        assertEquals("회원가입 처리 실패 | users 테이블이 없습니다.", response.body?.error?.message)
    }

    @Test
    fun `데이터 제약 조건 위반은 400 응답으로 변환한다`() {
        val response = handler.handleDataIntegrityViolationException(DataIntegrityViolationException("중복된 이메일입니다."))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertFalse(response.body?.success ?: true)
        assertEquals(ErrorCode.BAD_REQUEST.name, response.body?.error?.code)
        assertEquals("데이터 처리 중 요청이 올바르지 않습니다.", response.body?.error?.message)
    }
}
