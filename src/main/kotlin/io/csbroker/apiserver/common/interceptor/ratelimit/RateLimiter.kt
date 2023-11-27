package io.csbroker.apiserver.common.interceptor.ratelimit

import jakarta.servlet.http.HttpServletRequest

interface RateLimiter {
    fun resolveRate(request: HttpServletRequest): Boolean
}
