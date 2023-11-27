package io.csbroker.apiserver.service.auth

import io.csbroker.apiserver.dto.auth.TokenDto
import io.csbroker.apiserver.dto.user.UserInfoDto
import io.csbroker.apiserver.dto.user.UserLoginRequestDto
import io.csbroker.apiserver.dto.user.UserSignUpDto
import jakarta.servlet.http.HttpServletRequest
import java.util.UUID

interface AuthService {
    fun saveUser(userDto: UserSignUpDto): UUID
    fun loginUser(userLoginRequestDto: UserLoginRequestDto): UserInfoDto
    fun refreshUserToken(request: HttpServletRequest): TokenDto
    fun changePassword(code: String, password: String): Boolean
}
