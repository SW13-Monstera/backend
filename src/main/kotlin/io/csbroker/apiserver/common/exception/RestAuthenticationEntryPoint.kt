package io.csbroker.apiserver.common.exception

import io.csbroker.apiserver.common.enums.ErrorCode
import io.csbroker.apiserver.common.util.log
import net.minidev.json.JSONObject
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
        log.info("Responding with unauthorized error. Message ${authException.message}")
        setResponse(response)
    }

    private fun setResponse(response: HttpServletResponse) {
        response.contentType = "application/json;charset=UTF-8"
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        val responseJson = JSONObject()
        responseJson["status"] = "fail"
        responseJson["data"] = ErrorCode.UNAUTHORIZED.message
        response.writer.print(responseJson)
    }
}
