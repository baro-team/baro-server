package com.baro.common.web.error

import com.baro.common.core.exception.BaroException
import com.baro.common.core.exception.BadRequestException
import com.baro.common.core.exception.ExternalServiceException
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.RestClientException

@AutoConfiguration
@RestControllerAdvice
class CommonRestExceptionHandler {
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<ErrorResponse> =
        ResponseEntity.badRequest().body(ErrorResponse(e.message ?: "잘못된 요청입니다."))

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequestException(e: BadRequestException): ResponseEntity<ErrorResponse> =
        ResponseEntity.badRequest().body(ErrorResponse(e.message ?: "잘못된 요청입니다."))

    @ExceptionHandler(ExternalServiceException::class, RestClientException::class)
    fun handleExternalServiceException(e: RuntimeException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_GATEWAY)
            .body(ErrorResponse(e.message ?: "외부 서비스 호출에 실패했습니다."))

    @ExceptionHandler(BaroException::class)
    fun handleBaroException(e: BaroException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(e.message ?: "서버 오류가 발생했습니다."))
}
