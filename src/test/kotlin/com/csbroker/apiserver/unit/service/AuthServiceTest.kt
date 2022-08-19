package com.csbroker.apiserver.unit.service

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
import com.csbroker.apiserver.dto.user.UserLoginRequestDto
import com.csbroker.apiserver.dto.user.UserSignUpDto
import com.csbroker.apiserver.model.User
import com.csbroker.apiserver.repository.common.RedisRepository
import com.csbroker.apiserver.repository.UserRepository
import com.csbroker.apiserver.service.AuthServiceImpl
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.Date
import java.util.UUID

class AuthServiceTest {
    private val userRepository: UserRepository = mockk()
    private val redisRepository: RedisRepository = mockk(relaxed = true)
    private val authTokenProvider: AuthTokenProvider = mockk()
    private val appProperties: AppProperties = mockk()
    private val passwordEncoder: BCryptPasswordEncoder = mockk()
    private val authService: AuthServiceImpl =
        AuthServiceImpl(userRepository, redisRepository, authTokenProvider, appProperties, passwordEncoder)

    private val user: User = User(
        id = UUID.randomUUID(),
        email = "test@test.com",
        password = "test1234!",
        username = "test",
        providerType = ProviderType.LOCAL
    )

    private val githubAdminUser: User = User(
        id = UUID.randomUUID(),
        email = "test@test.com",
        password = "test1234!",
        username = "test",
        role = Role.ROLE_ADMIN,
        providerType = ProviderType.GITHUB
    )

    private val now = Date()

    private val accessToken = Jwts.builder()
        .setSubject("test@test.com")
        .signWith(Keys.hmacShaKeyFor("thisissecret1234secretsecedsaeasfafsfasfsaafssaffsasfas1314124".toByteArray()))
        .claim(AUTHORITIES_KEY, Role.ROLE_USER)
        .setExpiration(Date(now.time + 1_800_000))
        .compact()

    private val refreshToken = Jwts.builder()
        .setSubject("test@test.com")
        .signWith(Keys.hmacShaKeyFor("thisissecret1234secretsecedsaeasfafsfasfsaafssaffsasfas1314124".toByteArray()))
        .setExpiration(Date(now.time + 604_800_000))
        .compact()

    @Test
    fun `회원 가입 성공 테스트`() {
        // given
        every { userRepository.save(any()) } returns user
        every { userRepository.findByEmailOrUsername("test@test.com", "test") } returns null
        every { passwordEncoder.encode(any()) } returns "encodedPassword"

        // when
        val result = authService.saveUser(UserSignUpDto("test@test.com", "test", "test1234!"))

        // then
        verify(exactly = 1) { userRepository.save(any()) }
        verify(exactly = 1) { userRepository.findByEmailOrUsername("test@test.com", "test") }
        verify(exactly = 1) { passwordEncoder.encode("test1234!") }
        Assertions.assertThat(user.id).isEqualTo(result)
    }

    @Test
    fun `회원 가입 이메일 중복 실패 테스트`() {
        // given
        every { userRepository.findByEmailOrUsername("test@test.com", "test1") } returns user

        // when
        val exception = assertThrows<ConditionConflictException> {
            authService.saveUser(UserSignUpDto("test@test.com", "test1", "test1234!"))
        }

        // then
        verify(exactly = 1) { userRepository.findByEmailOrUsername("test@test.com", "test1") }
        Assertions.assertThat(ErrorCode.EMAIL_DUPLICATED).isEqualTo(exception.errorCode)
    }

    @Test
    fun `회원 가입 닉네임 중복 실패 테스트`() {
        // given
        every { userRepository.findByEmailOrUsername("test1@test.com", "test") } returns user

        // when
        val exception = assertThrows<ConditionConflictException> {
            authService.saveUser(UserSignUpDto("test1@test.com", "test", "test1234!"))
        }

        // then
        verify(exactly = 1) { userRepository.findByEmailOrUsername("test1@test.com", "test") }
        Assertions.assertThat(ErrorCode.USERNAME_DUPLICATED).isEqualTo(exception.errorCode)
    }

    @Test
    fun `유저 정보 조회 테스트`() {
        // given
        every { userRepository.findByEmail("test@test.com") } returns user

        // when
        val userInfo = authService.getUserInfo("test@test.com")

        // then
        verify(exactly = 1) { userRepository.findByEmail("test@test.com") }
        Assertions.assertThat("test@test.com").isEqualTo(userInfo.email)
    }

    @Test
    fun `유저 정보 조회 실패 테스트`() {
        // given
        every { userRepository.findByEmail("test1@test.com") } returns null

        // when
        val exception = assertThrows<EntityNotFoundException> {
            authService.getUserInfo("test1@test.com")
        }

        // then
        verify(exactly = 1) { userRepository.findByEmail("test1@test.com") }
        Assertions.assertThat(ErrorCode.NOT_FOUND_ENTITY).isEqualTo(exception.errorCode)
    }

    @Test
    fun `로그인 성공 테스트`() {
        // given
        every { userRepository.findByEmail("test@test.com") } returns user
        every { passwordEncoder.matches("test1234!", any()) } returns true
        every { appProperties.auth.tokenExpiry } returns 1_800_000
        every { appProperties.auth.refreshTokenExpiry } returns 604_800_000
        every {
            authTokenProvider.createAuthToken(
                "test@test.com",
                any(),
                Role.ROLE_USER.code
            ).token
        } returns accessToken
        every { authTokenProvider.createAuthToken("test@test.com", any(), null).token } returns refreshToken

        // when
        val loginUser = authService.loginUser(UserLoginRequestDto("test@test.com", "test1234!"))

        // then
        verify(exactly = 1) { userRepository.findByEmail("test@test.com") }
        verify(exactly = 1) { passwordEncoder.matches("test1234!", any()) }
        verify(exactly = 1) { authTokenProvider.createAuthToken("test@test.com", any(), Role.ROLE_USER.code).token }
        verify(exactly = 1) { authTokenProvider.createAuthToken("test@test.com", any(), null).token }
        Assertions.assertThat(loginUser.email).isEqualTo("test@test.com")
        Assertions.assertThat(loginUser.username).isEqualTo("test")
        Assertions.assertThat(loginUser.role).isEqualTo(Role.ROLE_USER)
        Assertions.assertThat(loginUser.accessToken).isEqualTo(accessToken)
        Assertions.assertThat(loginUser.refreshToken).isEqualTo(refreshToken)
    }

    @Test
    fun `로그인 찾을 수 없는 회원 실패 테스트`() {
        // given
        every { userRepository.findByEmail("test@test.com") } returns null
        // when
        val exception = assertThrows<EntityNotFoundException> {
            authService.loginUser(UserLoginRequestDto("test@test.com", "test1234!"))
        }

        // then
        verify(exactly = 1) { userRepository.findByEmail("test@test.com") }
        Assertions.assertThat(ErrorCode.NOT_FOUND_ENTITY).isEqualTo(exception.errorCode)
    }

    @Test
    fun `로그인 OAuth 유저 실패 테스트`() {
        // given
        every { userRepository.findByEmail("test@test.com") } returns githubAdminUser
        // when
        val exception = assertThrows<OAuthProviderMissMatchException> {
            authService.loginUser(UserLoginRequestDto("test@test.com", "test1234!"))
        }

        // then
        verify(exactly = 1) { userRepository.findByEmail("test@test.com") }
        Assertions.assertThat(ErrorCode.PROVIDER_MISS_MATCH).isEqualTo(exception.errorCode)
    }

    @Test
    fun `로그인 비밀번호 불일치 실패 테스트`() {
        // given
        every { userRepository.findByEmail("test@test.com") } returns user
        every { passwordEncoder.matches("test", any()) } returns false

        // when
        val exception = assertThrows<UnAuthorizedException> {
            authService.loginUser(UserLoginRequestDto("test@test.com", "test"))
        }

        // then
        verify(exactly = 1) { userRepository.findByEmail("test@test.com") }
        Assertions.assertThat(ErrorCode.PASSWORD_MISS_MATCH).isEqualTo(exception.errorCode)
    }
}
