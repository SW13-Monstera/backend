package io.csbroker.apiserver.common.util

import jakarta.servlet.http.HttpServletRequest

const val HEADER_AUTHORIZATION = "Authorization"
const val TOKEN_PREFIX = "Bearer "

fun HttpServletRequest.getAccessToken(): String? {
    val headerValue = this.getHeader(HEADER_AUTHORIZATION)
    return if (headerValue?.startsWith(TOKEN_PREFIX) == true) headerValue.substring(TOKEN_PREFIX.length) else null
}
