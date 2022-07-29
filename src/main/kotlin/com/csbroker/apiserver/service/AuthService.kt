package com.csbroker.apiserver.service

import com.csbroker.apiserver.dto.auth.TokenDto
import com.csbroker.apiserver.dto.user.UserLoginDto
import com.csbroker.apiserver.dto.user.UserLoginRequestDto
import com.csbroker.apiserver.dto.user.UserSignUpDto
import com.csbroker.apiserver.model.User
import javax.servlet.http.HttpServletRequest

interface AuthService {
    fun saveUser(userDto: UserSignUpDto): User
    fun loginUser(userLoginRequestDto: UserLoginRequestDto): UserLoginDto
    fun refreshUserToken(request: HttpServletRequest): TokenDto
}
