package io.csbroker.apiserver.service

import io.csbroker.apiserver.auth.ProviderType
import io.csbroker.apiserver.common.enums.Role
import io.csbroker.apiserver.dto.user.UserUpdateRequestDto
import io.csbroker.apiserver.model.User
import io.csbroker.apiserver.repository.common.RedisRepository
import io.csbroker.apiserver.repository.problem.GradingHistoryRepository
import io.csbroker.apiserver.repository.user.UserRepository
import io.csbroker.apiserver.service.user.UserService
import io.csbroker.apiserver.service.user.UserServiceImpl
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.UUID

class UserServiceTest {
    private lateinit var userRepository: UserRepository
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var gradingHistoryRepository: GradingHistoryRepository
    private lateinit var redisRepository: RedisRepository
    private lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        userRepository = mockk()
        passwordEncoder = mockk()
        gradingHistoryRepository = mockk()
        redisRepository = mockk()
        userService = UserServiceImpl(userRepository, passwordEncoder, gradingHistoryRepository, redisRepository)
    }

    @Test
    fun `유저 정보 이메일 조회 성공 테스트`() {
        // given
        val user = createUser()
        every { userRepository.findByEmail(any()) } returns user

        // when
        val userInfo = userService.findUserByEmail("test@test.com")

        // then
        verify(exactly = 1) { userRepository.findByEmail(any()) }
        assertThat(userInfo).isNotNull
        assertThat("test@test.com").isEqualTo(userInfo!!.email)
    }

    @Test
    fun `유저 정보 이메일 조회 불가 테스트`() {
        // given
        every { userRepository.findByEmail(any()) } returns null

        // when
        val userInfo = userService.findUserByEmail("test1@test.com")

        // then
        verify(exactly = 1) { userRepository.findByEmail(any()) }
        assertThat(userInfo).isNull()
    }

    @Test
    fun `유저 정보 ID 조회 성공 테스트`() {
        // given
        val user = createUser()
        every { userRepository.findByIdOrNull(any()) } returns user

        // when
        val userInfo = userService.findUserById(user.id!!)

        // then
        verify(exactly = 1) { userRepository.findByIdOrNull(any()) }
        assertThat(userInfo).isNotNull
        assertThat(user.id).isEqualTo(userInfo!!.id)
    }

    @Test
    fun `유저 정보 ID 조회 불가 테스트`() {
        // given
        val id = UUID.randomUUID()
        every { userRepository.findByIdOrNull(any()) } returns null

        // when
        val userInfo = userService.findUserById(id)

        // then
        verify(exactly = 1) { userRepository.findByIdOrNull(any()) }
        assertThat(userInfo).isNull()
    }

    @Test
    fun `다수 유저 조회 테스트`() {
        // given
        val user = createUser()
        val githubAdminUser = createGithubAdminUser()
        every { userRepository.findAll() } returns listOf(user, githubAdminUser)

        // when
        val users = userService.findUsers()

        // then
        verify(exactly = 1) { userRepository.findAll() }
        assertThat(users.size).isEqualTo(2)
    }

    @Test
    fun `다수 어드민 유저 조회 테스트`() {
        // given
        val githubAdminUser = createGithubAdminUser()
        every { userRepository.findUsersByRole(any()) } returns listOf(githubAdminUser)

        // when
        val adminUsers = userService.findAdminUsers()

        // then
        verify(exactly = 1) { userRepository.findUsersByRole(any()) }
        assertThat(adminUsers.size).isEqualTo(1)
    }

    @Test
    fun `유저 수정 성공 without password 테스트`() {
        // given
        val user = createUser()
        val id = user.id!!
        val userUpdateRequestDto = UserUpdateRequestDto("test-url.com", "test", null, null)
        every { userRepository.findByIdOrNull(any()) } returns user

        // when
        val modifyUser = userService.modifyUser(
            id,
            user,
            userUpdateRequestDto,
        )

        // then
        assertThat("test-url.com").isEqualTo(modifyUser.profileImageUrl)
        assertThat("test").isEqualTo(modifyUser.username)
        assertThat(user.password).isEqualTo(modifyUser.password)
    }

    @Test
    fun `유저 수정 성공 with password 테스트`() {
        // given
        val user = createUser()
        val id = user.id!!
        val userUpdateRequestDto = UserUpdateRequestDto("test-url.com", "test", "test1234!", "test123!")
        every { userRepository.findByIdOrNull(any()) } returns user
        every { passwordEncoder.encode(any()) } returns "some-encrypted-password"
        every { passwordEncoder.matches(any(), any()) } returns true

        // when
        val modifyUser = userService.modifyUser(
            id,
            user,
            userUpdateRequestDto,
        )

        // then
        verify(exactly = 1) { passwordEncoder.encode(any()) }
        verify(exactly = 1) { passwordEncoder.matches(any(), any()) }
        assertThat("test-url.com").isEqualTo(modifyUser.profileImageUrl)
        assertThat("test").isEqualTo(modifyUser.username)
        assertThat("test123!").isNotEqualTo(modifyUser.password)
    }

    private fun createUser() = User(
        id = UUID.randomUUID(),
        email = "test@test.com",
        password = "test1234!",
        username = "test",
        providerType = ProviderType.LOCAL,
    )

    private fun createGithubAdminUser() = User(
        id = UUID.randomUUID(),
        email = "test@test.com",
        password = "test1234!",
        username = "test",
        role = Role.ROLE_ADMIN,
        providerType = ProviderType.GITHUB,
    )
}
