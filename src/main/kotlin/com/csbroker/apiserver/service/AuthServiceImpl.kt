package com.csbroker.apiserver.service

import com.csbroker.apiserver.auth.AUTHORITIES_KEY
import com.csbroker.apiserver.auth.AuthTokenProvider
import com.csbroker.apiserver.auth.ProviderType
import com.csbroker.apiserver.common.config.properties.AppProperties
import com.csbroker.apiserver.common.enums.ErrorCode
import com.csbroker.apiserver.common.enums.Role
import com.csbroker.apiserver.common.exception.ConditionConflictException
import com.csbroker.apiserver.common.exception.EntityNotFoundException
import com.csbroker.apiserver.common.exception.OAuthProviderMissMatchException
import com.csbroker.apiserver.common.exception.UnAuthorizedException
import com.csbroker.apiserver.common.util.getAccessToken
import com.csbroker.apiserver.common.util.getCookie
import com.csbroker.apiserver.dto.auth.TokenDto
import com.csbroker.apiserver.dto.user.UserInfoDto
import com.csbroker.apiserver.dto.user.UserLoginRequestDto
import com.csbroker.apiserver.dto.user.UserSignUpDto
import com.csbroker.apiserver.repository.UserRepository
import com.csbroker.apiserver.repository.common.REFRESH_TOKEN
import com.csbroker.apiserver.repository.common.RedisRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Date
import java.util.UUID
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
    override fun saveUser(userDto: UserSignUpDto): UUID {
        val findUser = userRepository.findByEmailOrUsername(userDto.email, userDto.username)

        if (findUser != null) {
            if (findUser.email == userDto.email) {
                throw ConditionConflictException(ErrorCode.EMAIL_DUPLICATED, "${userDto.email}은 중복 이메일입니다.")
            } else {
                throw ConditionConflictException(
                    ErrorCode.USERNAME_DUPLICATED,
                    "${userDto.username}은 중복 이메일입니다."
                )
            }
        }

        val user = userDto.toUser()
        val encodedPassword = passwordEncoder.encode(user.password)
        user.encodePassword(encodedPassword)

        return userRepository.save(user).id!!
    }

    override fun getUserInfo(email: String): UserInfoDto {
        val findUser = this.userRepository.findByEmail(email)
            ?: throw EntityNotFoundException("$email 을 가진 유저는 존재하지 않습니다.")

        return UserInfoDto(findUser)
    }

    override fun loginUser(userLoginRequestDto: UserLoginRequestDto): UserInfoDto {
        val email = userLoginRequestDto.email
        val rawPassword = userLoginRequestDto.password

        val findUser = userRepository.findByEmail(email)
            ?: throw EntityNotFoundException("$email 을 가진 유저는 존재하지 않습니다.")

        if (findUser.providerType != ProviderType.LOCAL) {
            throw OAuthProviderMissMatchException(
                "${findUser.email} 유저는 ${findUser.providerType} 를 통해 가입한 계정입니다."
            )
        }

        if (!passwordEncoder.matches(rawPassword, findUser.password)) {
            throw UnAuthorizedException(ErrorCode.PASSWORD_MISS_MATCH, "비밀번호가 일치하지 않습니다!")
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

        return UserInfoDto(findUser, accessToken, refreshToken)
    }

    override fun refreshUserToken(request: HttpServletRequest): TokenDto {
        val accessToken = getAccessToken(request)
            ?: throw UnAuthorizedException(ErrorCode.ACCESS_TOKEN_NOT_EXIST, "Access Token이 존재하지 않습니다.")

        val convertAccessToken = authTokenProvider.convertAuthToken(accessToken)

        val claims = convertAccessToken.expiredTokenClaims
            ?: throw UnAuthorizedException(ErrorCode.TOKEN_NOT_EXPIRED, "Access Token이 만료되지 않았거나 올바르지 않습니다.")

        val email = claims.subject
        val role = Role.of(claims.get(AUTHORITIES_KEY, String::class.java))

        val refreshToken = getCookie(request, REFRESH_TOKEN)?.value
            ?: throw UnAuthorizedException(ErrorCode.REFRESH_TOKEN_NOT_EXIST, "Refresh Token이 존재하지 않습니다.")

        val convertRefreshToken = authTokenProvider.convertAuthToken(refreshToken)

        if (!convertRefreshToken.isValid) {
            throw UnAuthorizedException(ErrorCode.TOKEN_INVALID, "올바르지 않은 토큰입니다. ( $refreshToken )")
        }

        val savedRefreshToken = redisRepository.getRefreshTokenByEmail(email)

        if (savedRefreshToken == null || savedRefreshToken != refreshToken) {
            throw UnAuthorizedException(ErrorCode.TOKEN_MISS_MATCH, "Refresh Token이 올바르지 않습니다.")
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

    @Transactional
    override fun changePassword(code: String, password: String): Boolean {
        val email = redisRepository.getEmailByCode(code)
            ?: throw UnAuthorizedException(ErrorCode.UNAUTHORIZED, "비밀번호를 변경할 수 없습니다.")

        val findUser = userRepository.findByEmail(email)
            ?: throw EntityNotFoundException("$email 을 가진 유저는 존재하지 않습니다.")

        findUser.encodePassword(passwordEncoder.encode(password))

        return true
    }
}
