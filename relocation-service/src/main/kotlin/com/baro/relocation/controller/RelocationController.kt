package com.baro.relocation.controller

import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/relocation")
class RelocationController {

    @PostMapping("/standWeights")
    @Operation(summary = "가중치 데이터 수신", description = "private에서 산출한 가중치 수신 / 저장")
    fun getWeights(): String {

        return "test"
    }
}