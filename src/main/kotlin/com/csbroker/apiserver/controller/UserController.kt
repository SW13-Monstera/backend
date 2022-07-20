package com.csbroker.apiserver.controller

import com.csbroker.apiserver.common.auth.LoginUser
import com.csbroker.apiserver.dto.ApiResponse
import com.csbroker.apiserver.dto.UserResponseDto
import com.csbroker.apiserver.service.UserService
import org.springframework.security.core.userdetails.User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {

    @GetMapping
    fun getUser(@LoginUser loginUser: User): ApiResponse<UserResponseDto> {
        val findUser = this.userService.findUserByEmail(loginUser.username)
            ?: throw IllegalArgumentException("${loginUser.username} is not appropriate email")

        return ApiResponse.success(findUser.toUserResponseDto())
    }
}
