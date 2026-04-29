package com.baro.common.core.exception

open class BaroException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

open class BadRequestException(
    message: String,
    cause: Throwable? = null,
) : BaroException(message, cause)

open class ExternalServiceException(
    message: String,
    cause: Throwable? = null,
) : BaroException(message, cause)
