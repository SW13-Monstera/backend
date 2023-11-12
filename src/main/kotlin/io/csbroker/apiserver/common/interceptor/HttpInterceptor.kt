package io.csbroker.apiserver.common.interceptor

import com.fasterxml.jackson.databind.ObjectMapper
import io.csbroker.apiserver.common.interceptor.ratelimit.RateLimiter
import io.csbroker.apiserver.common.util.setJsonResponseBody
import io.csbroker.apiserver.common.util.setStatus
import io.csbroker.apiserver.dto.common.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.web.servlet.HandlerInterceptor
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class HttpInterceptor(
    private val rateLimiters: List<RateLimiter>,
) : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (rateLimiters.all { it.resolveRate(request) }) {
            return true
        }

        response.setTooManyRequest()
        return false
    }

    private fun HttpServletResponse.setTooManyRequest() {
        this.setStatus(HttpStatus.TOO_MANY_REQUESTS)
        this.setJsonResponseBody(
            ObjectMapper().writeValueAsBytes(
                ApiResponse.fail("짧은 시간 동안 너무 많이 요청하였습니다."),
            ),
        )
    }
}
