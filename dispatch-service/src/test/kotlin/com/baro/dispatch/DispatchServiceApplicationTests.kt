package com.baro.dispatch

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
    properties = [
        "baro.jwt.secret=test-jwt-secret-must-be-at-least-32-bytes",
    ],
)
class DispatchServiceApplicationTests {

    @Test
    fun `스프링 컨텍스트가 정상적으로 로드된다`() {
    }
}
