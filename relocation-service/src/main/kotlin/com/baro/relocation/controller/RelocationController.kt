package com.baro.relocation.controller

import com.baro.relocation.dto.RelocationAssignRequest
import com.baro.relocation.dto.RelocationAssignResponse
import com.baro.relocation.dto.VehicleCompleteRequest
import com.baro.relocation.service.RelocationService
import com.baro.relocation.service.RelocationTriggerService
import io.swagger.v3.oas.annotations.Operation
import org.slf4j.LoggerFactory
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
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @PostMapping("/assign")
    @Operation(summary = "차량 재배치 택시 정거장 할당", description = "운행이 끝난 차량들 재배치할 택시 정거장 위치 반환")
    fun assignRelocation(
        @RequestBody request: RelocationAssignRequest,
    ): ResponseEntity<RelocationAssignResponse> {
        val response = relocationService.assignRelocation(request)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/internal/vehicle-completed")
    fun vehicleCompleted(@RequestBody request: VehicleCompleteRequest): ResponseEntity<Void> {
        CompletableFuture.runAsync {
            try {
                relocationTriggerService.triggerRelocation(request.carId, request.lat, request.lon)
            } catch (e: Exception) {
                log.warn("재배치 트리거 실패 (무시). carId={}", request.carId, e)
            }
        }
        return ResponseEntity.accepted().build()
    }
}
