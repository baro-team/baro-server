package com.baro.relocation

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
    properties = [
        "RELOCATION_DB_URL=jdbc:h2:mem:testdb",
        "RELOCATION_DB_USERNAME=sa",
        "RELOCATION_DB_PASSWORD=",
        "CONTROL_SERVICE_URL=http://localhost:8081",
        "spring.jpa.hibernate.ddl-auto=none",
    ],
)
class SaveWeightServiceApplicationTests {
    @Test
    fun `스프링 컨텍스트가 정상적으로 로드된다`() {
    }
}
