package com.baro.relocation.dto

import io.swagger.v3.oas.annotations.media.Schema

data class RelocationAssignRequest(
    @Schema(description = "차량 ID")
    val carId: Long,
    
    @Schema(description = "차량 현재 위도")
    val currentLat: Double,
    
    @Schema(description = "차량 현재 경도")
    val currentLon: Double
)
