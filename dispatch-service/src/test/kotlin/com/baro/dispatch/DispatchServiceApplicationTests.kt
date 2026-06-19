package com.baro.dispatch

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
    properties = [
        "baro.jwt.secret=test-jwt-secret-must-be-at-least-32-bytes",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
        "DISPATCH_DB_URL=jdbc:h2:mem:testdb",
        "DISPATCH_DB_USERNAME=sa",
        "DISPATCH_DB_PASSWORD=",
        "spring.jpa.hibernate.ddl-auto=none",
    ],
)
class DispatchServiceApplicationTests {

    @Test
    fun `스프링 컨텍스트가 정상적으로 로드된다`() {
    }
}
