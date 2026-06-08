package com.baro.relocation.dto

import io.swagger.v3.oas.annotations.media.Schema

data class RelocationAssignResponse(
    @Schema(description = "차량 ID")
    val carId: Long,
    
    @Schema(description = "재배치 정거장 ID")
    val targetStandId: String,
    
    @Schema(description = "재배치 정거장 위도")
    val targetLat: Double,
    
    @Schema(description = "재배치 정거장 경도")
    val targetLon: Double
)
