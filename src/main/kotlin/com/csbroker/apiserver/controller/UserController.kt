package com.csbroker.apiserver.controller

import com.csbroker.apiserver.dto.ApiResponse
import com.csbroker.apiserver.service.UserService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {

    @GetMapping
    fun getUser(): ApiResponse<Any> {
        val principal =
            SecurityContextHolder.getContext().authentication.principal
                as org.springframework.security.core.userdetails.User

        val findUser = this.userService.findUserByEmail(principal.username)
            ?: return ApiResponse.fail(mapOf("email" to "${principal.username} is not appropriate email"))

        return ApiResponse.success(findUser)
    }
}
