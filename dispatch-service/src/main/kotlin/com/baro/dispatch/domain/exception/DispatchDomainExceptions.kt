package com.baro.dispatch.domain.exception

import com.baro.common.core.exception.BadRequestException

class RouteNotFoundException(message: String) : BadRequestException(message)
