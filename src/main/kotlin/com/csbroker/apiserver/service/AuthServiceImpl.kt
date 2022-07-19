package com.csbroker.apiserver.service

import com.csbroker.apiserver.common.auth.AUTHORITIES_KEY
import com.csbroker.apiserver.common.auth.AuthTokenProvider
import com.csbroker.apiserver.common.config.properties.AppProperties
import com.csbroker.apiserver.common.enums.Role
import com.csbroker.apiserver.common.util.getAccessToken
import com.csbroker.apiserver.common.util.getCookie
import com.csbroker.apiserver.dto.TokenDto
import com.csbroker.apiserver.dto.UserLoginRequestDto
import com.csbroker.apiserver.dto.UserSignUpDto
import com.csbroker.apiserver.model.User
import com.csbroker.apiserver.repository.REFRESH_TOKEN
import com.csbroker.apiserver.repository.RedisRepository
import com.csbroker.apiserver.repository.UserRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.util.Date
import javax.servlet.http.HttpServletRequest

private const val THREE_DAYS_MSEC = 259200000

@Service
class AuthServiceImpl(
    private val userRepository: UserRepository,
    private val redisRepository: RedisRepository,
    private val authTokenProvider: AuthTokenProvider,
    private val appProperties: AppProperties,
    private val passwordEncoder: BCryptPasswordEncoder
) : AuthService {
    override fun saveUser(userDto: UserSignUpDto): User {
        val findUser = userRepository.findByEmailOrUsername(userDto.email, userDto.username)

        if (findUser != null) {
            throw IllegalArgumentException("User already exists with email or id")
        }

        val user = userDto.toUser()
        val encodedPassword = passwordEncoder.encode(user.password)
        user.encodePassword(encodedPassword)

        return userRepository.save(user)
    }

    override fun loginUser(userLoginRequestDto: UserLoginRequestDto): TokenDto {
        val email = userLoginRequestDto.email
        val rawPassword = userLoginRequestDto.password

        val findUser = userRepository.findByEmail(email)
            ?: throw IllegalArgumentException("존재하지 않는 이메일입니다. $email")

        if (!passwordEncoder.matches(rawPassword, findUser.password)) {
            throw IllegalArgumentException("비밀번호가 일치하지 않습니다!")
        }

        val role = findUser.role

        val now = Date()
        val tokenExpiry = appProperties.auth.tokenExpiry
        val refreshTokenExpiry = appProperties.auth.refreshTokenExpiry

        val accessToken = authTokenProvider.createAuthToken(
            email,
            Date(now.time + tokenExpiry),
            role.code
        ).token

        val refreshToken = authTokenProvider.createAuthToken(
            email,
            Date(now.time + refreshTokenExpiry)
        ).token

        redisRepository.setRefreshTokenByEmail(email, refreshToken)

        return TokenDto(accessToken, refreshToken)
    }

    override fun refreshUserToken(request: HttpServletRequest): TokenDto {
        val accessToken = getAccessToken(request)
            ?: throw IllegalArgumentException("Access Token이 존재하지 않습니다.")

        val convertAuthToken = authTokenProvider.convertAuthToken(accessToken)

        val claims = convertAuthToken.expiredTokenClaims
            ?: throw IllegalArgumentException("Access Token이 만료되지 않았거나 올바르지 않습니다.")

        val email = claims.subject
        val role = Role.of(claims.get(AUTHORITIES_KEY, String::class.java))

        val refreshToken = getCookie(request, REFRESH_TOKEN)?.value
            ?: throw IllegalArgumentException("Refresh Token이 존재하지 않습니다.")

        val convertRefreshToken = authTokenProvider.convertAuthToken(refreshToken)

        if (!convertRefreshToken.isValid) {
            throw IllegalArgumentException("Refresh Token이 올바르지 않습니다.")
        }

        val savedRefreshToken = redisRepository.getRefreshTokenByEmail(email)

        if (savedRefreshToken == null || savedRefreshToken != refreshToken) {
            throw IllegalArgumentException("Refresh Token이 올바르지 않습니다.")
        }

        val now = Date()
        val tokenExpiry = appProperties.auth.tokenExpiry

        val newAccessToken = authTokenProvider.createAuthToken(
            email,
            Date(now.time + tokenExpiry),
            role.code
        ).token

        val validTime = convertRefreshToken.tokenClaims!!.expiration.time - now.time

        if (validTime <= THREE_DAYS_MSEC) {
            val refreshTokenExpiry = appProperties.auth.refreshTokenExpiry
            val newRefreshToken = authTokenProvider.createAuthToken(
                email,
                Date(now.time + refreshTokenExpiry)
            ).token

            return TokenDto(newAccessToken, newRefreshToken)
        }

        return TokenDto(newAccessToken, null)
    }
}
