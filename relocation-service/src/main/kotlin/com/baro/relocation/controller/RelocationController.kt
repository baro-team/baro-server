package com.baro.relocation.controller

import com.baro.relocation.dto.RelocationAssignRequest
import com.baro.relocation.dto.RelocationAssignResponse
import com.baro.relocation.dto.VehicleCompleteRequest
import com.baro.relocation.service.RelocationService
import com.baro.relocation.service.RelocationTriggerService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.slf4j.LoggerFactory
import org.springframework.core.task.TaskExecutor
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.CompletableFuture

@RestController
@RequestMapping("/relocation")
class RelocationController(
    private val relocationService: RelocationService,
    private val relocationTriggerService: RelocationTriggerService,
    private val taskExecutor: TaskExecutor,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @PostMapping("/assign")
    @Operation(summary = "차량 재배치 택시 정거장 할당", description = "운행이 끝난 차량들 재배치할 택시 정거장 위치 반환")
    @SecurityRequirement(name = "bearerAuth")
    fun assignRelocation(
        @RequestBody request: RelocationAssignRequest,
    ): ResponseEntity<RelocationAssignResponse> {
        val response = relocationService.assignRelocation(request)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/internal/vehicle-completed")
    fun vehicleCompleted(@RequestBody request: VehicleCompleteRequest): ResponseEntity<Void> {
        log.info("재배치 트리거 요청 수신. carId={} lat={} lon={}", request.carId, request.lat, request.lon)
        try {
            CompletableFuture.runAsync({
                try {
                    relocationTriggerService.triggerRelocation(request.carId, request.lat, request.lon)
                } catch (e: Exception) {
                    log.error("재배치 트리거 실패. carId={}", request.carId, e)
                }
            }, taskExecutor)
        } catch (e: Exception) {
            log.error("재배치 트리거 비동기 작업 제출 실패. carId={}", request.carId, e)
            return ResponseEntity.status(503).build()
        }
        return ResponseEntity.accepted().build()
    }
}
