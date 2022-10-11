package io.csbroker.apiserver.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import javax.servlet.http.HttpServletResponse

@Controller
class CustomErrorController(
    @Value("\${server.error.redirect}")
    private val redirectUrl: String
) : ErrorController {

    @RequestMapping("/error")
    fun redirectError(response: HttpServletResponse) {
        response.sendRedirect(redirectUrl)
    }
}
