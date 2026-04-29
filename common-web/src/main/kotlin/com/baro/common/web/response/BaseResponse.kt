package com.baro.common.web.response

data class BaseResponse<T>(
    val success: Boolean,
    val data: T?,
    val error: ErrorResponse?,
) {
    companion object {
        fun <T> success(data: T): BaseResponse<T> =
            BaseResponse(
                success = true,
                data = data,
                error = null,
            )

        fun error(code: ErrorCode, message: String): BaseResponse<Nothing> =
            BaseResponse(
                success = false,
                data = null,
                error = ErrorResponse(code = code.name, message = message),
            )
    }
}

data class ErrorResponse(
    val code: String,
    val message: String,
)

enum class ErrorCode {
    BAD_REQUEST,
    EXTERNAL_SERVICE_ERROR,
    INTERNAL_SERVER_ERROR,
}
