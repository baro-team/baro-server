package com.baro.dispatch.interfaces.rest

import com.baro.dispatch.application.service.CancelDispatchResult
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class CancelDispatchResponse(
    @field:JsonProperty("dispatch_id")
    @field:Schema(name = "dispatch_id", description = "배차 ID", example = "10")
    val dispatchId: Long,
    @field:JsonProperty("request_id")
    @field:Schema(name = "request_id", description = "PRE배차 요청 ID", example = "1")
    val requestId: Long,
    @field:JsonProperty("car_id")
    @field:Schema(name = "car_id", description = "차량 ID", example = "101")
    val carId: Long,
    @field:JsonProperty("car_number")
    @field:Schema(name = "car_number", description = "차량번호", example = "12가3456")
    val carNumber: String?,
    @field:JsonProperty("dispatch_status")
    @field:Schema(name = "dispatch_status", description = "배차 상태", example = "CANCELLED")
    val dispatchStatus: String,
) {
    companion object {
        fun from(result: CancelDispatchResult): CancelDispatchResponse =
            CancelDispatchResponse(
                dispatchId = result.dispatchId,
                requestId = result.requestId,
                carId = result.carId,
                carNumber = result.carNumber,
                dispatchStatus = result.status,
            )
    }
}
