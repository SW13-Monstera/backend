package com.csbroker.apiserver.service

import com.csbroker.apiserver.dto.TokenDto
import com.csbroker.apiserver.dto.UserLoginDto
import com.csbroker.apiserver.dto.UserLoginRequestDto
import com.csbroker.apiserver.dto.UserSignUpDto
import com.csbroker.apiserver.model.User
import javax.servlet.http.HttpServletRequest

interface AuthService {
    fun saveUser(userDto: UserSignUpDto): User
    fun loginUser(userLoginRequestDto: UserLoginRequestDto): UserLoginDto
    fun refreshUserToken(request: HttpServletRequest): TokenDto
}
