package com.csbroker.apiserver.controller

import com.csbroker.apiserver.common.auth.LoginUser
import com.csbroker.apiserver.dto.ApiResponse
import com.csbroker.apiserver.dto.UserResponseDto
import com.csbroker.apiserver.dto.UserUpdateRequestDto
import com.csbroker.apiserver.service.UserService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.userdetails.User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'BUSINESS')")
    fun getUser(@LoginUser loginUser: User, @PathVariable("id") id: UUID): ApiResponse<UserResponseDto> {
        val findUser = this.userService.findUserById(id)
            ?: throw IllegalArgumentException("$id is not appropriate id")

        if (findUser.email != loginUser.username) {
            throw IllegalArgumentException("${loginUser.username} is not appropriate email")
        }

        return ApiResponse.success(findUser.toUserResponseDto())
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun getUsers(): ApiResponse<List<UserResponseDto>> {
        val result = this.userService.findUsers()
            .map(com.csbroker.apiserver.model.User::toUserResponseDto)
        return ApiResponse.success(result)
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'BUSINESS')")
    fun updateUser(
        @LoginUser loginUser: User,
        @PathVariable("id") id: UUID,
        @RequestBody userUpdateRequestDto: UserUpdateRequestDto
    ): ApiResponse<UserResponseDto> {
        val findUser = this.userService.findUserById(id)

        if (findUser == null || findUser.email != loginUser.username) {
            throw IllegalArgumentException("옳지 않은 권한 혹은 id입니다.")
        }

        val user = this.userService.modifyUser(id, userUpdateRequestDto)
            ?: throw IllegalArgumentException("문제가 생겨 업데이트를 하지 못하였습니다.")

        return ApiResponse.success(user.toUserResponseDto())
    }
}
