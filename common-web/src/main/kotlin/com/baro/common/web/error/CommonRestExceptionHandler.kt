package com.baro.common.web.error

import com.baro.common.core.exception.BaroException
import com.baro.common.core.exception.BadRequestException
import com.baro.common.core.exception.ExternalServiceException
import com.baro.common.web.response.BaseResponse
import com.baro.common.web.response.ErrorCode
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
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<BaseResponse<Nothing>> =
        ResponseEntity.badRequest().body(BaseResponse.error(ErrorCode.BAD_REQUEST, e.message ?: "잘못된 요청입니다."))

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequestException(e: BadRequestException): ResponseEntity<BaseResponse<Nothing>> =
        ResponseEntity.badRequest().body(BaseResponse.error(ErrorCode.BAD_REQUEST, e.message ?: "잘못된 요청입니다."))

    @ExceptionHandler(ExternalServiceException::class, RestClientException::class)
    fun handleExternalServiceException(e: RuntimeException): ResponseEntity<BaseResponse<Nothing>> =
        ResponseEntity.status(HttpStatus.BAD_GATEWAY)
            .body(BaseResponse.error(ErrorCode.EXTERNAL_SERVICE_ERROR, e.message ?: "외부 서비스 호출에 실패했습니다."))

    @ExceptionHandler(BaroException::class)
    fun handleBaroException(e: BaroException): ResponseEntity<BaseResponse<Nothing>> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(BaseResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, e.message ?: "서버 오류가 발생했습니다."))

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<BaseResponse<Nothing>> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(BaseResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."))
}
