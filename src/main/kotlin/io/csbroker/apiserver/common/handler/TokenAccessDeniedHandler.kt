package io.csbroker.apiserver.common.handler

import io.csbroker.apiserver.common.enums.ErrorCode
import net.minidev.json.JSONObject
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

@Component
class TokenAccessDeniedHandler : AccessDeniedHandler {
    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException,
    ) {
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
