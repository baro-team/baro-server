package com.baro.dispatch.interfaces.rest

import com.baro.common.web.response.BaseResponse
import com.baro.common.web.response.ErrorCode
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class DispatchRestExceptionHandler {
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(e: AccessDeniedException): ResponseEntity<BaseResponse<Nothing>> =
        ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(BaseResponse.error(ErrorCode.FORBIDDEN, e.message ?: "접근 권한이 없습니다."))
}
