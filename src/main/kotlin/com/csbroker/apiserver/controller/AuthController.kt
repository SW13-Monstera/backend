package com.csbroker.apiserver.controller

import com.csbroker.apiserver.common.config.properties.AppProperties
import com.csbroker.apiserver.common.util.addCookie
import com.csbroker.apiserver.common.util.deleteCookie
import com.csbroker.apiserver.dto.ApiResponse
import com.csbroker.apiserver.dto.TokenResponseDto
import com.csbroker.apiserver.dto.UserLoginRequestDto
import com.csbroker.apiserver.dto.UserResponseDto
import com.csbroker.apiserver.dto.UserSignUpDto
import com.csbroker.apiserver.repository.REFRESH_TOKEN
import com.csbroker.apiserver.service.AuthService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val appProperties: AppProperties
) {

    @PostMapping("/signup")
    fun signUp(@RequestBody userSignUpDto: UserSignUpDto): ApiResponse<UserResponseDto> {
        val userResponseDto = this.authService.saveUser(userSignUpDto)
            .toUserResponseDto()

        return ApiResponse.success(userResponseDto)
    }

    @PostMapping("/login")
    fun login(
        request: HttpServletRequest,
        response: HttpServletResponse,
        @RequestBody userLoginRequestDto: UserLoginRequestDto
    ): ApiResponse<TokenResponseDto> {
        val (accessToken, refreshToken) = this.authService.loginUser(userLoginRequestDto)

        val cookieMaxAge = (appProperties.auth.refreshTokenExpiry / 60).toInt()

        deleteCookie(request, response, REFRESH_TOKEN)
        addCookie(response, REFRESH_TOKEN, refreshToken!!, cookieMaxAge)

        return ApiResponse.success(TokenResponseDto(accessToken))
    }

    @GetMapping("/refresh")
    fun refresh(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ApiResponse<TokenResponseDto> {
        val (accessToken, refreshToken) = this.authService.refreshUserToken(request)

        if (refreshToken != null) {
            val cookieMaxAge = (appProperties.auth.refreshTokenExpiry / 60).toInt()

            deleteCookie(request, response, REFRESH_TOKEN)
            addCookie(response, REFRESH_TOKEN, refreshToken, cookieMaxAge)
        }

        return ApiResponse.success(TokenResponseDto(accessToken))
    }
}
