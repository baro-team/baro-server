package com.baro.user.interfaces.rest

object AuthApiPaths {
    private const val USER_PREFIX = "/user"

    const val SIGN_UP = "$USER_PREFIX/auth/sign-up"
    const val LOGIN = "$USER_PREFIX/auth/login"
    const val REFRESH = "$USER_PREFIX/auth/token/refresh"
    const val LOGOUT = "$USER_PREFIX/auth/logout"
    const val ME = "$USER_PREFIX/users/me"
}
