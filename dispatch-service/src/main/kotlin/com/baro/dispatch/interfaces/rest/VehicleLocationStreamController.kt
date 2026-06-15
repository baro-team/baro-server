package com.baro.dispatch.interfaces.rest

import com.baro.dispatch.application.service.VehicleLocationStreamService
import com.baro.dispatch.domain.repository.DispatchRepository
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping(DispatchApiPaths.DISPATCH)
class VehicleLocationStreamController(
    private val dispatchRepository: DispatchRepository,
    private val vehicleLocationStreamService: VehicleLocationStreamService,
) {
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{dispatchId}/vehicle-location/stream", produces = ["text/event-stream"])
    fun stream(
        @PathVariable dispatchId: Long,
        @AuthenticationPrincipal jwt: Jwt,
    ): SseEmitter {
        val authenticatedUserId = jwt.subject?.toLongOrNull()
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 사용자 정보가 올바르지 않습니다.")
        val dispatch = dispatchRepository.findById(dispatchId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "배차를 찾을 수 없습니다.")

        if (dispatch.userId != authenticatedUserId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "배차 위치 스트림 접근 권한이 없습니다.")
        }

        return vehicleLocationStreamService.subscribe(dispatchId, dispatch.carId)
    }
}
