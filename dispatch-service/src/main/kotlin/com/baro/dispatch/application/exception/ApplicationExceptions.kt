package com.baro.dispatch.application.exception

import com.baro.common.core.exception.ExternalServiceException

class ExternalRouteException(message: String, cause: Throwable? = null) : ExternalServiceException(message, cause)
