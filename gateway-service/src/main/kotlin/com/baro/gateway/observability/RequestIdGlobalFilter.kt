package com.baro.gateway.observability

import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.util.UUID

@Component
class RequestIdGlobalFilter : GlobalFilter, Ordered {
    private val allowedRequestId = Regex("^[A-Za-z0-9._:-]{1,128}$")

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val currentRequestId = exchange.request.headers.getFirst(GatewayRequestHeaders.REQUEST_ID)
        val requestId = currentRequestId?.takeIf { allowedRequestId.matches(it) }

        if (requestId != null) {
            exchange.response.headers.set(GatewayRequestHeaders.REQUEST_ID, requestId)
            return chain.filter(exchange)
        }

        val newRequestId = UUID.randomUUID().toString()
        val request = exchange.request.mutate()
            .headers { it.set(GatewayRequestHeaders.REQUEST_ID, newRequestId) }
            .build()

        exchange.response.headers.set(GatewayRequestHeaders.REQUEST_ID, newRequestId)

        return chain.filter(exchange.mutate().request(request).build())
    }

    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE
}
