package io.csbroker.apiserver.common.interceptor.ratelimit

interface RateLimiter {
    fun resolveRate(key: String): Boolean
}
