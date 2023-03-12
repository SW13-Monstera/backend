package io.csbroker.apiserver.common.interceptor.ratelimit

import javax.servlet.http.HttpServletRequest

interface RateLimiter {
    fun resolveRate(request: HttpServletRequest): Boolean
}
