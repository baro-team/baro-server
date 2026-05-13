package com.baro.user.domain.exception

import com.baro.common.core.exception.BadRequestException

class DuplicateEmailException(message: String = "이미 사용 중인 이메일입니다.") : BadRequestException(message)

class InvalidCredentialsException(message: String = "이메일 또는 비밀번호가 올바르지 않습니다.") : BadRequestException(message)

class UserNotFoundException(message: String = "사용자를 찾을 수 없습니다.") : BadRequestException(message)

class InvalidRefreshTokenException(message: String = "유효하지 않은 토큰입니다.") : BadRequestException(message)
