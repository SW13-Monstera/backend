package com.csbroker.apiserver.common.exception

import com.csbroker.apiserver.common.util.log
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class RestAuthenticationEntryPoint : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        authException.printStackTrace()
        log.info("Responding with unauthorized error. Message ${authException.message}")
        response.sendError(
            HttpServletResponse.SC_UNAUTHORIZED,
            authException.localizedMessage
        )
    }
}
