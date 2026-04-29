package com.baro.dispatch.interfaces.rest

import com.baro.common.web.response.BaseResponse
import com.baro.dispatch.application.service.PreDispatchService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(DispatchApiPaths.DISPATCH)
class PreDispatchController(
    private val preDispatchService: PreDispatchService,
) {
    @Operation(
        summary = "사전 배차 예상",
        description = "출발지와 도착지 좌표를 받아 예상 요금, 경로, 소요 시간을 계산합니다.",
    )
    @PostMapping(DispatchApiPaths.PRE_DISPATCH)
    fun estimate(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "건국대학교에서 홍익대학교(북위)로 이동하는 예시 요청",
            required = true,
            content = [
                Content(
                    mediaType = "application/json",
                    examples = [
                        ExampleObject(
                            name = "건국대 -> 홍익대",
                            value = """
                                {
                                  "user_id": 1001,
                                  "origin": {
                                    "lat": 37.547,
                                    "lon": 127.091896,
                                    "name": "건국대학교"
                                  },
                                  "destination": {
                                    "lat": 37.551464,
                                    "lon": 126.925011,
                                    "name": "홍익대학교"
                                  }
                                }
                            """,
                        ),
                    ],
                ),
            ],
        )
        @RequestBody request: PreDispatchRequest,
    ): BaseResponse<PreDispatchResponse> =
        BaseResponse.success(PreDispatchResponse.from(preDispatchService.estimate(request.toCommand())))
}
