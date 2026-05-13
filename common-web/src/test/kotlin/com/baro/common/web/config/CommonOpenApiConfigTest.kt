package com.baro.common.web.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

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

    @Test
    fun `공통 OpenAPI 설정은 JWT Bearer 인증 스키마를 추가한다`() {
        val openApi = CommonOpenApiConfig().baroOpenApi(BaroOpenApiProperties())

        val securityScheme = openApi.components.securitySchemes["bearerAuth"]

        assertNotNull(securityScheme)
        assertEquals("http", securityScheme.type.toString())
        assertEquals("bearer", securityScheme.scheme)
        assertEquals("JWT", securityScheme.bearerFormat)
    }
}
