package com.csbroker.apiserver.service

import com.csbroker.apiserver.dto.auth.TokenDto
import com.csbroker.apiserver.dto.user.UserInfoDto
import com.csbroker.apiserver.dto.user.UserLoginRequestDto
import com.csbroker.apiserver.dto.user.UserSignUpDto
import java.util.UUID
import javax.servlet.http.HttpServletRequest

interface AuthService {
    fun saveUser(userDto: UserSignUpDto): UUID
    fun loginUser(userLoginRequestDto: UserLoginRequestDto): UserInfoDto
    fun refreshUserToken(request: HttpServletRequest): TokenDto
    fun getUserInfo(email: String): UserInfoDto
}
