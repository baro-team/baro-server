package com.baro.relocation.dto

import io.swagger.v3.oas.annotations.media.Schema

data class RelocationAssignRequest(
    @Schema(description = "차량 ID")
    val carId: Long,
    
    @Schema(description = "차량 현재 위도")
    val currentLat: Double,
    
    @Schema(description = "차량 현재 경도")
    val currentLon: Double,

    @Schema(description = "PostGIS 사용 여부 : false 설정 시 기존 In-Memory 하버사인 방식으로 연산", defaultValue = "true")
    val usePostGis: Boolean = true
)
