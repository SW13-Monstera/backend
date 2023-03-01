package io.csbroker.apiserver.common.interceptor.ratelimit

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.Bucket4j
import io.github.bucket4j.Refill
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

class IpBasedRateLimiter(
    private val rateLimit: Long,
) : BaseRateLimiter(
    ConcurrentHashMap<String, Bucket>(),
) {
    override fun resolveRate(key: String): Boolean {
        return super.resolveRate(key, key.getConsumeCountByIp()) {
            createNewBucket()
        }
    }

    private fun String.getConsumeCountByIp(): Long {
        // TODO : 특정 ip 대역에 따라 무제한 허용 or 무제한 접근 금지
        return 1
    }

    private fun createNewBucket() = Bucket4j.builder()
        .addLimit(Bandwidth.classic(rateLimit, Refill.intervally(rateLimit, Duration.ofSeconds(1))))
        .build()
}
