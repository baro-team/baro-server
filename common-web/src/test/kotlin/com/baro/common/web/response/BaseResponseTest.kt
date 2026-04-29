package com.baro.common.web.response

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BaseResponseTest {
    @Test
    fun `성공 응답은 데이터만 포함한다`() {
        val response = BaseResponse.success(TestData(id = 1L, name = "강남"))

        assertTrue(response.success)
        assertEquals(TestData(id = 1L, name = "강남"), response.data)
        assertNull(response.error)
    }

    @Test
    fun `에러 응답은 에러 코드와 메시지만 포함한다`() {
        val response = BaseResponse.error(ErrorCode.BAD_REQUEST, "잘못된 요청입니다.")

        assertFalse(response.success)
        assertNull(response.data)
        assertEquals("BAD_REQUEST", response.error?.code)
        assertEquals("잘못된 요청입니다.", response.error?.message)
    }

    private data class TestData(
        val id: Long,
        val name: String,
    )
}
