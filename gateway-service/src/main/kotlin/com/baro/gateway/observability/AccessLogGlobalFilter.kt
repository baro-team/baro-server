package com.baro.gateway.observability

import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR
import org.springframework.core.Ordered
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.Instant

@Component
class AccessLogGlobalFilter : GlobalFilter, Ordered {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val startedAt = Instant.now()

        return chain.filter(exchange)
            .doFinally {
                val request = exchange.request
                val response = exchange.response
                val routeId = exchange.getAttribute<org.springframework.cloud.gateway.route.Route>(GATEWAY_ROUTE_ATTR)?.id
                    ?: "unmatched"
                val requestId = request.headers.getFirst(GatewayRequestHeaders.REQUEST_ID) ?: "unknown"
                val status = response.statusCode?.value() ?: 0
                val latencyMs = Duration.between(startedAt, Instant.now()).toMillis()

                log.info(
                    "gateway_access request_id={} method={} path={} route_id={} status={} latency_ms={}",
                    requestId,
                    request.method.name(),
                    request.path.value(),
                    routeId,
                    status,
                    latencyMs,
                )
            }
    }

    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE + 1
}
