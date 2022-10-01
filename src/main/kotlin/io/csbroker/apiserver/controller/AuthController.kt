package io.csbroker.apiserver.controller

import io.csbroker.apiserver.auth.LoginUser
import io.csbroker.apiserver.common.config.properties.AppProperties
import io.csbroker.apiserver.common.util.addCookie
import io.csbroker.apiserver.common.util.deleteCookie
import io.csbroker.apiserver.dto.auth.PasswordChangeMailRequestDto
import io.csbroker.apiserver.dto.auth.PasswordChangeRequestDto
import io.csbroker.apiserver.dto.auth.TokenResponseDto
import io.csbroker.apiserver.dto.common.ApiResponse
import io.csbroker.apiserver.dto.common.UpsertSuccessResponseDto
import io.csbroker.apiserver.dto.user.UserInfoResponseDto
import io.csbroker.apiserver.dto.user.UserLoginRequestDto
import io.csbroker.apiserver.dto.user.UserSignUpDto
import io.csbroker.apiserver.repository.common.REFRESH_TOKEN
import io.csbroker.apiserver.service.AuthService
import io.csbroker.apiserver.service.MailService
import kotlinx.coroutines.runBlocking
import org.springframework.security.core.userdetails.User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
    private val appProperties: AppProperties,
    private val mailService: MailService
) {

    @PostMapping("/signup")
    fun signUp(@RequestBody userSignUpDto: UserSignUpDto): ApiResponse<UpsertSuccessResponseDto> {
        val userId = this.authService.saveUser(userSignUpDto)
        return ApiResponse.success(UpsertSuccessResponseDto(id = userId))
    }

    @GetMapping("/info")
    fun getUserInfo(@LoginUser loginUser: User): ApiResponse<UserInfoResponseDto> {
        val userInfo = this.authService.getUserInfo(loginUser.username)
        return ApiResponse.success(UserInfoResponseDto(userInfo))
    }

    @PostMapping("/login")
    fun login(
        request: HttpServletRequest,
        response: HttpServletResponse,
        @RequestBody userLoginRequestDto: UserLoginRequestDto
    ): ApiResponse<UserInfoResponseDto> {
        val userInfoDto = this.authService.loginUser(userLoginRequestDto)

        val cookieMaxAge = appProperties.auth.refreshTokenExpiry / 1000

        deleteCookie(request, response, REFRESH_TOKEN)
        addCookie(response, REFRESH_TOKEN, userInfoDto.refreshToken!!, cookieMaxAge)

        return ApiResponse.success(UserInfoResponseDto(userInfoDto))
    }

    @GetMapping("/refresh")
    fun refresh(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ApiResponse<TokenResponseDto> {
        val (accessToken, refreshToken) = this.authService.refreshUserToken(request)

        if (refreshToken != null) {
            val cookieMaxAge = appProperties.auth.refreshTokenExpiry / 1000

            deleteCookie(request, response, REFRESH_TOKEN)
            addCookie(response, REFRESH_TOKEN, refreshToken, cookieMaxAge)
        }

        return ApiResponse.success(TokenResponseDto(accessToken))
    }

    @PostMapping("/password/code")
    fun sendPasswordChangeMail(
        @RequestBody passwordChangeMailRequestDto: PasswordChangeMailRequestDto
    ): ApiResponse<String> {
        return runBlocking {
            mailService.sendPasswordChangeMail(passwordChangeMailRequestDto.email)
            ApiResponse.success("success")
        }
    }

    @PutMapping("/password/change")
    fun changePassword(
        @RequestBody passwordChangeRequestDto: PasswordChangeRequestDto
    ): ApiResponse<String> {
        authService.changePassword(passwordChangeRequestDto.code, passwordChangeRequestDto.password)
        return ApiResponse.success("success")
    }
}
