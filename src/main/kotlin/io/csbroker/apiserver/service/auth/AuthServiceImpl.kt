package io.csbroker.apiserver.service.auth

import io.csbroker.apiserver.auth.AuthTokenProvider
import io.csbroker.apiserver.common.config.properties.AppProperties
import io.csbroker.apiserver.common.enums.ErrorCode
import io.csbroker.apiserver.common.enums.Role
import io.csbroker.apiserver.common.exception.ConditionConflictException
import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.common.exception.UnAuthorizedException
import io.csbroker.apiserver.common.util.getAccessToken
import io.csbroker.apiserver.common.util.getCookie
import io.csbroker.apiserver.dto.auth.TokenDto
import io.csbroker.apiserver.dto.user.UserInfoDto
import io.csbroker.apiserver.dto.user.UserLoginRequestDto
import io.csbroker.apiserver.dto.user.UserSignUpDto
import io.csbroker.apiserver.model.User
import io.csbroker.apiserver.repository.common.REFRESH_TOKEN
import io.csbroker.apiserver.repository.common.RedisRepository
import io.csbroker.apiserver.repository.user.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
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
    private val passwordEncoder: PasswordEncoder,
) : AuthService {
    override fun saveUser(userDto: UserSignUpDto): UUID {
        checkEmailAndUserName(userDto.email, userDto.username)

        val user = userDto.toUser()
        val encodedPassword = passwordEncoder.encode(user.password)
        user.encodePassword(encodedPassword)

        return userRepository.save(user).id!!
    }

    override fun getUserInfo(email: String): UserInfoDto {
        val findUser = userRepository.findByEmail(email)
            ?: throw EntityNotFoundException("$email 을 가진 유저는 존재하지 않습니다.")

        return UserInfoDto(findUser)
    }

    override fun loginUser(userLoginRequestDto: UserLoginRequestDto): UserInfoDto {
        val email = userLoginRequestDto.email
        val rawPassword = userLoginRequestDto.password

        val findUser = userRepository.findByEmail(email)
            ?: throw EntityNotFoundException("$email 을 가진 유저는 존재하지 않습니다.")

        if (!passwordEncoder.matches(rawPassword, findUser.password)) {
            throw UnAuthorizedException(ErrorCode.PASSWORD_MISS_MATCH, "비밀번호가 일치하지 않습니다!")
        }

        val (accessToken, refreshToken) = createTokens(findUser, email)
        return UserInfoDto(findUser, accessToken, refreshToken)
    }

    private fun createTokens(
        findUser: User,
        email: String,
    ): Pair<String, String> {
        val role = findUser.role

        val now = Date()
        val tokenExpiry = appProperties.auth.tokenExpiry
        val refreshTokenExpiry = appProperties.auth.refreshTokenExpiry

        val accessToken = authTokenProvider.createAuthToken(
            email,
            Date(now.time + tokenExpiry),
            role.code,
        ).token

        val refreshToken = authTokenProvider.createAuthToken(
            email,
            Date(now.time + refreshTokenExpiry),
        ).token

        redisRepository.setRefreshTokenByEmail(email, refreshToken)

        return accessToken to refreshToken
    }

    override fun refreshUserToken(request: HttpServletRequest): TokenDto {
        val accessToken = getAccessToken(request)
            ?: throw UnAuthorizedException(ErrorCode.ACCESS_TOKEN_NOT_EXIST, "Access Token이 존재하지 않습니다.")

        val convertAccessToken = authTokenProvider.convertAuthToken(accessToken)

        val claims = convertAccessToken.expiredTokenClaims
            ?: throw UnAuthorizedException(ErrorCode.TOKEN_NOT_EXPIRED, "Access Token이 만료되지 않았거나 올바르지 않습니다.")

        val email = claims.subject
        val role = Role.of(claims.get(io.csbroker.apiserver.auth.AUTHORITIES_KEY, String::class.java))

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
            role.code,
        ).token

        val validTime = convertRefreshToken.tokenClaims!!.expiration.time - now.time

        if (validTime <= THREE_DAYS_MSEC) {
            val refreshTokenExpiry = appProperties.auth.refreshTokenExpiry
            val newRefreshToken = authTokenProvider.createAuthToken(
                email,
                Date(now.time + refreshTokenExpiry),
            ).token

            return TokenDto(newAccessToken, newRefreshToken)
        }

        return TokenDto(newAccessToken, null)
    }

    @Transactional
    override fun changePassword(code: String, password: String): Boolean {
        val email = redisRepository.getEmailByCode(code)
            ?: throw UnAuthorizedException(ErrorCode.FORBIDDEN, "비밀번호를 변경할 수 없습니다.")

        val findUser = userRepository.findByEmail(email)
            ?: throw EntityNotFoundException("$email 을 가진 유저는 존재하지 않습니다.")

        findUser.encodePassword(passwordEncoder.encode(password))
        redisRepository.removePasswordVerification(code)

        return true
    }

    private fun checkEmailAndUserName(email: String, username: String) {
        userRepository.findByEmailOrUsername(email, username)?.let {
            if (it.email == email) {
                throw ConditionConflictException(ErrorCode.EMAIL_DUPLICATED, "$email 은 중복 이메일입니다.")
            }
            throw ConditionConflictException(
                ErrorCode.USERNAME_DUPLICATED,
                "$username 은 중복 닉네임입니다.",
            )
        }
    }
}
