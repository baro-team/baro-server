package com.baro.user.interfaces.rest

import com.baro.common.web.response.BaseResponse
import com.baro.common.web.response.ErrorCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(basePackages = ["com.baro.user"])
class UserRestExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<BaseResponse<Nothing>> {
        val message = e.bindingResult.fieldErrors.firstOrNull()?.defaultMessage ?: "잘못된 요청입니다."
        return ResponseEntity.badRequest().body(BaseResponse.error(ErrorCode.BAD_REQUEST, message))
    }
}
