package com.baro.dispatch.application.service

import com.baro.dispatch.domain.model.DispatchStatus
import java.time.OffsetDateTime

data class DispatchExportDto(
    val createdAt: OffsetDateTime,
    val requestId: Long,
    val userId: Long,
    val startLatitude: Double,
    val startLongitude: Double,
    val endLatitude: Double,
    val endLongitude: Double,
    val status: DispatchStatus
)
