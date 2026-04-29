package com.baro.common.core.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

class BaroExceptionsTest {
    @Test
    fun `공통 예외는 메시지와 원인을 유지한다`() {
        val cause = IllegalStateException("원인")
        val exception = BadRequestException("잘못된 요청입니다.", cause)

        assertIs<BaroException>(exception)
        assertEquals("잘못된 요청입니다.", exception.message)
        assertSame(cause, exception.cause)
    }
}
