package com.baro.common.web.error

import com.baro.common.core.exception.BaroException
import com.baro.common.core.exception.BadRequestException
import com.baro.common.core.exception.ExternalServiceException
import com.baro.common.web.response.BaseResponse
import com.baro.common.web.response.ErrorCode
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.RestClientException

@AutoConfiguration
@EnableConfigurationProperties(BaroErrorProperties::class)
@RestControllerAdvice
class CommonRestExceptionHandler(
    private val properties: BaroErrorProperties,
) {
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

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolationException(e: DataIntegrityViolationException): ResponseEntity<BaseResponse<Nothing>> =
        ResponseEntity.badRequest()
            .body(BaseResponse.error(ErrorCode.BAD_REQUEST, "데이터 처리 중 요청이 올바르지 않습니다."))

    @ExceptionHandler(BaroException::class)
    fun handleBaroException(e: BaroException): ResponseEntity<BaseResponse<Nothing>> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(BaseResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, serverErrorMessage(e)))

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<BaseResponse<Nothing>> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(BaseResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, serverErrorMessage(e)))

    private fun serverErrorMessage(e: Throwable): String {
        if (!properties.includeDetails) {
            return "서버 오류가 발생했습니다."
        }

        val detail = generateSequence(e) { it.cause }
            .mapNotNull { it.message?.takeIf(String::isNotBlank) }
            .distinct()
            .joinToString(" | ")

        return detail.ifBlank { "서버 오류가 발생했습니다." }
    }
}

@ConfigurationProperties(prefix = "baro.error")
data class BaroErrorProperties(
    val includeDetails: Boolean = false,
)
