package com.baro.dispatch.interfaces.rest

import com.baro.dispatch.application.service.PreDispatchService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(DispatchApiPaths.DISPATCH)
class PreDispatchController(
    private val preDispatchService: PreDispatchService,
) {
    @PostMapping(DispatchApiPaths.PRE_DISPATCH)
    fun estimate(@RequestBody request: PreDispatchRequest): PreDispatchResponse =
        PreDispatchResponse.from(preDispatchService.estimate(request.toCommand()))
}
