package io.csbroker.apiserver.common.interceptor

import com.fasterxml.jackson.databind.ObjectMapper
import io.csbroker.apiserver.common.interceptor.ratelimit.RateLimiter
import io.csbroker.apiserver.dto.common.ApiResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class HttpInterceptor(
    private val rateLimiter: RateLimiter,
) : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (rateLimiter.resolveRate(request.remoteAddr)) {
            return true
        }

        setBodyTooManyRequest(response)
        return false
    }

    private fun setBodyTooManyRequest(response: HttpServletResponse) {
        val errorResponse = ObjectMapper().writeValueAsBytes(
            ApiResponse.fail("짧은 시간 동안 너무 많이 요청하였습니다."),
        )
        response.setContentLength(errorResponse.size)
        response.outputStream.write(errorResponse)
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        response.status = HttpStatus.TOO_MANY_REQUESTS.value()
    }
}
