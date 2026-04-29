package com.baro.common.web.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import kotlin.test.Test
import kotlin.test.assertEquals

class CommonJacksonConfigTest {
    @Test
    fun `공통 Jackson 설정은 JSON 필드를 snake case로 변환한다`() {
        val builder = Jackson2ObjectMapperBuilder()
        CommonJacksonConfig().snakeCaseObjectMapper().customize(builder)
        val objectMapper = builder.createXmlMapper(false).build<ObjectMapper>()

        val json = objectMapper.writeValueAsString(TestResponse(routePath = listOf(1, 2), estimatedTime = 46))

        assertEquals("""{"route_path":[1,2],"estimated_time":46}""", json)
    }

    private data class TestResponse(
        val routePath: List<Int>,
        val estimatedTime: Int,
    )
}
