package com.baro.dispatch.interfaces.rest

import com.baro.dispatch.application.exception.ExternalRouteException
import com.baro.dispatch.domain.exception.RouteNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.RestClientException

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<Map<String, String>> =
        ResponseEntity.badRequest().body(mapOf("message" to (e.message ?: "잘못된 요청입니다.")))

    @ExceptionHandler(RouteNotFoundException::class)
    fun handleRouteNotFoundException(e: RouteNotFoundException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("message" to (e.message ?: "경로를 찾을 수 없습니다.")))

    @ExceptionHandler(ExternalRouteException::class, RestClientException::class)
    fun handleExternalRouteException(e: RuntimeException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.BAD_GATEWAY)
            .body(mapOf("message" to (e.message ?: "경로 정보를 조회하지 못했습니다.")))
}
