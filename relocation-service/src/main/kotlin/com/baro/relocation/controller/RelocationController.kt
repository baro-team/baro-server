package com.baro.relocation.controller

import com.baro.relocation.dto.StandWeightRequest
import com.baro.relocation.dto.StandWeightResponse
import com.baro.relocation.service.RelocationService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/relocation")
class RelocationController (
    private val relocationService: RelocationService
) {
    @PostMapping("/standWeights")
    @Operation(summary = "가중치 데이터 수신", description = "private에서 산출한 가중치 수신 / 저장")
    fun receiveWeights(
        @RequestBody request: StandWeightRequest
    ): ResponseEntity<StandWeightResponse> {

        relocationService.saveWeights(request)

        return ResponseEntity.ok(
            StandWeightResponse(
                message = "가중치 저장 완료",
                count = request.weights.size
            )
        )
    }
}