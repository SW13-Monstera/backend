package io.csbroker.apiserver.common.config.security

import io.csbroker.apiserver.auth.LoginUserArgumentResolver
import io.csbroker.apiserver.common.interceptor.HttpInterceptor
import io.csbroker.apiserver.common.interceptor.ratelimit.ApiPathBasedRateLimiter
import io.csbroker.apiserver.common.interceptor.ratelimit.IpBasedRateLimiter
import io.csbroker.apiserver.common.interceptor.ratelimit.RateLimiter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMcvConfig(
    @Value("\${server.rate-limit}")
    private val rateLimit: Long,
    private val loginUserArgumentResolver: LoginUserArgumentResolver,
) : WebMvcConfigurer {
    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(loginUserArgumentResolver)
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(httpInterceptor())
    }

    @Bean
    fun ipBasedRateLimiter(): RateLimiter {
        return IpBasedRateLimiter(rateLimit)
    }

    @Bean
    fun apiPathBasedRateLimiter(): RateLimiter {
        return ApiPathBasedRateLimiter(listOf("/api/v1/chat"))
    }

    @Bean
    fun httpInterceptor(): HandlerInterceptor {
        return HttpInterceptor(listOf(ipBasedRateLimiter(), apiPathBasedRateLimiter()))
    }
}
