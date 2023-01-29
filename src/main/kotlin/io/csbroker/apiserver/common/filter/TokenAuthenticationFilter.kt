package io.csbroker.apiserver.common.filter

import io.csbroker.apiserver.common.util.getAccessToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class TokenAuthenticationFilter(
    private val tokenProvider: io.csbroker.apiserver.auth.AuthTokenProvider
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val tokenStr = getAccessToken(request)

        tokenStr ?: run {
            filterChain.doFilter(request, response)
            return
        }

        val token = tokenProvider.convertAuthToken(tokenStr)

        if (token.isValid) {
            val authentication = tokenProvider.getAuthentication(token)
            SecurityContextHolder.getContext().authentication = authentication
        }

        filterChain.doFilter(request, response)
    }
}
