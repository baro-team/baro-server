package com.baro.gateway.observability

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.http.server.reactive.ServerHttpRequest
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class RequestIdGlobalFilterTest {
    private val filter = RequestIdGlobalFilter()

    @Test
    fun `요청에 요청 ID가 없으면 새 값을 요청과 응답 헤더에 넣고 체인을 호출한다`() {
        val routed = AtomicBoolean(false)
        val forwardedRequest = AtomicReference<ServerHttpRequest>()
        val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/dispatch/pre"))

        StepVerifier.create(filter.filter(exchange, chain(routed, forwardedRequest)))
            .verifyComplete()

        assertTrue(routed.get())
        val requestId = forwardedRequest.get().headers.getFirst(GatewayRequestHeaders.REQUEST_ID)
        assertNotNull(requestId)
        assertEquals(requestId, exchange.response.headers.getFirst(GatewayRequestHeaders.REQUEST_ID))
    }

    @Test
    fun `요청에 요청 ID가 있으면 기존 값을 요청과 응답 헤더에 유지하고 체인을 호출한다`() {
        val routed = AtomicBoolean(false)
        val forwardedRequest = AtomicReference<ServerHttpRequest>()
        val exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/dispatch/pre")
                .header(GatewayRequestHeaders.REQUEST_ID, "request-123"),
        )

        StepVerifier.create(filter.filter(exchange, chain(routed, forwardedRequest)))
            .verifyComplete()

        assertTrue(routed.get())
        assertEquals("request-123", forwardedRequest.get().headers.getFirst(GatewayRequestHeaders.REQUEST_ID))
        assertEquals("request-123", exchange.response.headers.getFirst(GatewayRequestHeaders.REQUEST_ID))
    }

    @Test
    fun `요청에 너무 길거나 허용되지 않는 문자 요청 ID가 오면 새 요청 ID를 요청과 응답 헤더에 넣는다`() {
        val routed = AtomicBoolean(false)
        val forwardedRequest = AtomicReference<ServerHttpRequest>()
        val invalidRequestId = "a".repeat(129) + "!"
        val exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/dispatch/pre")
                .header(GatewayRequestHeaders.REQUEST_ID, invalidRequestId),
        )

        StepVerifier.create(filter.filter(exchange, chain(routed, forwardedRequest)))
            .verifyComplete()

        assertTrue(routed.get())
        val requestId = requireNotNull(forwardedRequest.get().headers.getFirst(GatewayRequestHeaders.REQUEST_ID))
        assertTrue(requestId != invalidRequestId)
        assertTrue(Regex("^[A-Za-z0-9._:-]{1,128}$").matches(requestId))
        assertEquals(requestId, exchange.response.headers.getFirst(GatewayRequestHeaders.REQUEST_ID))
    }

    private fun chain(
        routed: AtomicBoolean,
        forwardedRequest: AtomicReference<ServerHttpRequest>,
    ): GatewayFilterChain = GatewayFilterChain { exchange ->
        routed.set(true)
        forwardedRequest.set(exchange.request)
        Mono.empty()
    }
}
