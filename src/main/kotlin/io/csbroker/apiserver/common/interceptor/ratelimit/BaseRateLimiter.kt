package io.csbroker.apiserver.common.interceptor.ratelimit

import io.github.bucket4j.Bucket

abstract class BaseRateLimiter(
    private val bucketCache: MutableMap<String, Bucket>,
) : RateLimiter {
    protected fun resolveRate(key: String, consumeCnt: Long, createNewBucket: () -> Bucket): Boolean {
        return bucketCache.computeIfAbsent(key) {
            createNewBucket()
        }.tryConsume(consumeCnt)
    }
}
