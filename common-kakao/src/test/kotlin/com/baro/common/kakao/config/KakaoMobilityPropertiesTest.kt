package com.baro.common.kakao.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KakaoMobilityPropertiesTest {
    @Test
    fun `API 키가 비어 있으면 설정되지 않은 상태로 판단한다`() {
        val properties = KakaoMobilityProperties(apiKey = " ")

        assertFalse(properties.hasApiKey())
    }

    @Test
    fun `Authorization 헤더 값은 KakaoAK 접두어와 공백 제거된 키로 만든다`() {
        val properties = KakaoMobilityProperties(apiKey = " test-api-key ")

        assertTrue(properties.hasApiKey())
        assertEquals("KakaoAK test-api-key", properties.authorizationHeaderValue())
    }
}
