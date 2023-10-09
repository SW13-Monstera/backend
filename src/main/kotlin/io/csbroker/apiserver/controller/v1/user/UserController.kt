package io.csbroker.apiserver.controller.v1.user

import io.csbroker.apiserver.auth.LoginUser
import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.dto.common.ApiResponse
import io.csbroker.apiserver.dto.common.DeleteResponseDto
import io.csbroker.apiserver.dto.user.UserResponseDto
import io.csbroker.apiserver.dto.user.UserStatsDto
import io.csbroker.apiserver.dto.user.UserUpdateRequestDto
import io.csbroker.apiserver.model.User
import io.csbroker.apiserver.service.user.UserService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService,
) {
    @GetMapping("/{id}")
    fun getUser(@PathVariable("id") id: UUID): ApiResponse<UserResponseDto> {
        val findUser = userService.findUserById(id)
            ?: throw EntityNotFoundException("${id}를 가진 유저를 찾을 수 없습니다.")

        return ApiResponse.success(findUser.toUserResponseDto())
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun getUsers(): ApiResponse<List<UserResponseDto>> {
        val result = userService.findUsers()
            .map(User::toUserResponseDto)
        return ApiResponse.success(result)
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'BUSINESS')")
    fun updateUser(
        @LoginUser loginUser: User,
        @PathVariable("id") id: UUID,
        @RequestBody userUpdateRequestDto: UserUpdateRequestDto,
    ): ApiResponse<UserResponseDto> {
        val user = userService.modifyUser(id, loginUser, userUpdateRequestDto)
        return ApiResponse.success(user.toUserResponseDto())
    }

    @GetMapping("/{id}/stats")
    fun getUserStats(
        @PathVariable("id") id: UUID,
    ): ApiResponse<UserStatsDto> {
        return ApiResponse.success(userService.getStats(id))
    }

    @DeleteMapping("/{id}")
    fun deleteUser(
        @LoginUser loginUser: User,
        @PathVariable("id") id: UUID,
    ): ApiResponse<DeleteResponseDto> {
        val result = userService.deleteUser(loginUser, id)
        return ApiResponse.success(DeleteResponseDto(id, result))
    }
}
