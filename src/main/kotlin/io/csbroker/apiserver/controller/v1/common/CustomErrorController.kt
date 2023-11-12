package io.csbroker.apiserver.controller.v1.common

import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class CustomErrorController(
    @Value("\${server.error.redirect}")
    private val redirectUrl: String,
) : ErrorController {

    @RequestMapping("/error")
    fun redirectError(response: HttpServletResponse) {
        response.sendRedirect(redirectUrl)
    }
}
