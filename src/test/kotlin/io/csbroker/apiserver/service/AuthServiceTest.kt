package io.csbroker.apiserver.service

import io.csbroker.apiserver.auth.AUTHORITIES_KEY
import io.csbroker.apiserver.auth.AuthTokenProvider
import io.csbroker.apiserver.auth.ProviderType
import io.csbroker.apiserver.common.config.properties.AppProperties
import io.csbroker.apiserver.common.enums.ErrorCode
import io.csbroker.apiserver.common.enums.Role
import io.csbroker.apiserver.common.exception.ConditionConflictException
import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.common.exception.UnAuthorizedException
import io.csbroker.apiserver.dto.user.UserLoginRequestDto
import io.csbroker.apiserver.dto.user.UserSignUpDto
import io.csbroker.apiserver.model.User
import io.csbroker.apiserver.repository.common.RedisRepository
import io.csbroker.apiserver.repository.user.UserRepository
import io.csbroker.apiserver.service.auth.AuthService
import io.csbroker.apiserver.service.auth.AuthServiceImpl
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.Date
import java.util.UUID

class AuthServiceTest {
    private lateinit var userRepository: UserRepository
    private lateinit var redisRepository: RedisRepository
    private lateinit var authTokenProvider: AuthTokenProvider
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var sut: AuthService

    @BeforeEach
    fun setUp() {
        userRepository = mockk()
        redisRepository = mockk(relaxed = true)
        authTokenProvider = mockk()
        passwordEncoder = mockk()
        sut = AuthServiceImpl(
            userRepository,
            redisRepository,
            authTokenProvider,
            AppProperties(
                auth = AppProperties.Auth("secret", 1000L, 1000L),
                oAuth2 = AppProperties.OAuth2(listOf("http://localhost:8080")),
            ),
            passwordEncoder,
        )
    }

    @Test
    fun `회원 가입 성공 테스트`() {
        // given
        val user = createUser()
        every { userRepository.save(any()) } returns user
        every { userRepository.findByEmailOrUsername(any(), any()) } returns null
        every { passwordEncoder.encode(any()) } returns "encodedPassword"

        // when
        val result = sut.saveUser(UserSignUpDto("test@test.com", "test", "test1234!"))

        // then
        verify(exactly = 1) { userRepository.save(any()) }
        verify(exactly = 1) { userRepository.findByEmailOrUsername(any(), any()) }
        verify(exactly = 1) { passwordEncoder.encode(any()) }
        assertThat(user.id).isEqualTo(result)
    }

    @Test
    fun `회원 가입 이메일 중복 실패 테스트`() {
        // given
        val user = createUser()
        every { userRepository.findByEmailOrUsername(any(), any()) } returns user

        // when
        val exception = assertThrows<ConditionConflictException> {
            sut.saveUser(UserSignUpDto("test@test.com", "test1", "test1234!"))
        }

        // then
        verify(exactly = 1) { userRepository.findByEmailOrUsername(any(), any()) }
        assertThat(ErrorCode.EMAIL_DUPLICATED).isEqualTo(exception.errorCode)
    }

    @Test
    fun `회원 가입 닉네임 중복 실패 테스트`() {
        // given
        val user = createUser()
        every { userRepository.findByEmailOrUsername(any(), any()) } returns user

        // when
        val exception = assertThrows<ConditionConflictException> {
            sut.saveUser(UserSignUpDto("test1@test.com", "test", "test1234!"))
        }

        // then
        verify(exactly = 1) { userRepository.findByEmailOrUsername(any(), any()) }
        assertThat(ErrorCode.USERNAME_DUPLICATED).isEqualTo(exception.errorCode)
    }

    @Test
    fun `로그인 성공 테스트`() {
        // given
        val user = createUser()
        val accessToken = createAccessToken()
        val refreshToken = createRefreshToken()
        every { userRepository.findByEmail(any()) } returns user
        every { passwordEncoder.matches(any(), any()) } returns true
        every {
            authTokenProvider.createAuthToken(
                any(),
                any(),
                Role.ROLE_USER.code,
            ).token
        } returns accessToken
        every { authTokenProvider.createAuthToken(any(), any(), null).token } returns refreshToken

        // when
        val loginUser = sut.loginUser(UserLoginRequestDto("test@test.com", "test1234!"))

        // then
        verify(exactly = 1) { userRepository.findByEmail(any()) }
        verify(exactly = 1) { passwordEncoder.matches(any(), any()) }
        verify(exactly = 1) { authTokenProvider.createAuthToken(any(), any(), Role.ROLE_USER.code).token }
        verify(exactly = 1) { authTokenProvider.createAuthToken(any(), any(), null).token }
        assertThat(loginUser.email).isEqualTo("test@test.com")
        assertThat(loginUser.username).isEqualTo("test")
        assertThat(loginUser.role).isEqualTo(Role.ROLE_USER)
        assertThat(loginUser.accessToken).isEqualTo(accessToken)
        assertThat(loginUser.refreshToken).isEqualTo(refreshToken)
    }

    @Test
    fun `로그인 찾을 수 없는 회원 실패 테스트`() {
        // given
        every { userRepository.findByEmail(any()) } returns null

        // when
        val exception = assertThrows<EntityNotFoundException> {
            sut.loginUser(UserLoginRequestDto("test@test.com", "test1234!"))
        }

        // then
        verify(exactly = 1) { userRepository.findByEmail(any()) }
        assertThat(ErrorCode.NOT_FOUND_ENTITY).isEqualTo(exception.errorCode)
    }

    @Test
    fun `로그인 비밀번호 불일치 실패 테스트`() {
        // given
        val user = createUser()
        every { userRepository.findByEmail(any()) } returns user
        every { passwordEncoder.matches(any(), any()) } returns false

        // when
        val exception = assertThrows<UnAuthorizedException> {
            sut.loginUser(UserLoginRequestDto(user.email, user.password))
        }

        // then
        verify(exactly = 1) { userRepository.findByEmail(any()) }
        verify(exactly = 1) { passwordEncoder.matches(any(), any()) }
        assertThat(ErrorCode.PASSWORD_MISS_MATCH).isEqualTo(exception.errorCode)
    }

    private fun createUser() = User(
        id = UUID.randomUUID(),
        email = "test@test.com",
        password = "test1234!",
        username = "test",
        providerType = ProviderType.LOCAL,
    )

    private fun createAccessToken() = Jwts.builder()
        .setSubject("test@test.com")
        .signWith(Keys.hmacShaKeyFor("thisissecret1234secretsecedsaeasfafsfasfsaafssaffsasfas1314124".toByteArray()))
        .claim(AUTHORITIES_KEY, Role.ROLE_USER)
        .setExpiration(Date(Date().time + 1_800_000))
        .compact()

    private fun createRefreshToken() = Jwts.builder()
        .setSubject("test@test.com")
        .signWith(Keys.hmacShaKeyFor("thisissecret1234secretsecedsaeasfafsfasfsaafssaffsasfas1314124".toByteArray()))
        .setExpiration(Date(Date().time + 604_800_000))
        .compact()
}
