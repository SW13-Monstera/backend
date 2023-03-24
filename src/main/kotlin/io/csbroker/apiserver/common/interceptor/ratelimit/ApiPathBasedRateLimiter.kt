package io.csbroker.apiserver.common.interceptor.ratelimit

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.Bucket4j
import io.github.bucket4j.Refill
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import javax.servlet.http.HttpServletRequest

class ApiPathBasedRateLimiter(
    private val paths: List<String>,
) : BaseRateLimiter(
    ConcurrentHashMap<String, Bucket>(),
) {
    override fun resolveRate(request: HttpServletRequest): Boolean {
        return super.resolveRate(request.getKey(), request.getConsumeCount()) {
            createNewBucket()
        }
    }

    private fun HttpServletRequest.getKey(): String {
        return "${this.remoteAddr}-${this.requestURI}"
    }

    private fun HttpServletRequest.getConsumeCount(): Long {
        if (paths.contains(this.requestURI)) {
            return 1
        }
        return 0
    }

    private fun createNewBucket() = Bucket4j.builder()
        .addLimit(Bandwidth.classic(1, Refill.intervally(1, Duration.ofSeconds(1))))
        .build()
}
