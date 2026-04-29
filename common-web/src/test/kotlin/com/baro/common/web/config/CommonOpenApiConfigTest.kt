package com.baro.common.web.config

import kotlin.test.Test
import kotlin.test.assertEquals

class CommonOpenApiConfigTest {
    @Test
    fun `공통 OpenAPI 설정은 서비스 문서 정보를 반영한다`() {
        val openApi = CommonOpenApiConfig().baroOpenApi(
            BaroOpenApiProperties(
                title = "Dispatch API",
                description = "배차 서비스 API",
                version = "v1",
            ),
        )

        assertEquals("Dispatch API", openApi.info.title)
        assertEquals("배차 서비스 API", openApi.info.description)
        assertEquals("v1", openApi.info.version)
    }
}
