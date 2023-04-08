package io.csbroker.apiserver.service

import io.csbroker.apiserver.auth.ProviderType
import io.csbroker.apiserver.common.enums.ErrorCode
import io.csbroker.apiserver.common.enums.Role
import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.dto.user.UserUpdateRequestDto
import io.csbroker.apiserver.model.User
import io.csbroker.apiserver.repository.problem.GradingHistoryRepository
import io.csbroker.apiserver.repository.user.UserRepository
import io.csbroker.apiserver.repository.common.RedisRepository
import io.csbroker.apiserver.service.user.UserServiceImpl
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.UUID

class UserServiceTest {
    private val userRepository: UserRepository = mockk()
    private val passwordEncoder: BCryptPasswordEncoder = mockk()
    private val gradingHistoryRepository: GradingHistoryRepository = mockk()
    private val redisRepository: RedisRepository = mockk()
    private val userService: UserServiceImpl =
        UserServiceImpl(userRepository, passwordEncoder, gradingHistoryRepository, redisRepository)

    private val user: User = User(
        id = UUID.randomUUID(),
        email = "test@test.com",
        password = "test1234!",
        username = "test",
        providerType = ProviderType.LOCAL,
    )

    private val githubAdminUser: User = User(
        id = UUID.randomUUID(),
        email = "test@test.com",
        password = "test1234!",
        username = "test",
        role = Role.ROLE_ADMIN,
        providerType = ProviderType.GITHUB,
    )

    @Test
    fun `유저 정보 이메일 조회 성공 테스트`() {
        // given
        every { userRepository.findByEmail("test@test.com") } returns user

        // when
        val userInfo = userService.findUserByEmail("test@test.com")

        // then
        verify(exactly = 1) { userRepository.findByEmail("test@test.com") }
        Assertions.assertThat(userInfo).isNotNull
        Assertions.assertThat("test@test.com").isEqualTo(userInfo!!.email)
    }

    @Test
    fun `유저 정보 이메일 조회 불가 테스트`() {
        // given
        every { userRepository.findByEmail("test1@test.com") } returns null

        // when
        val userInfo = userService.findUserByEmail("test1@test.com")

        // then
        verify(exactly = 1) { userRepository.findByEmail("test1@test.com") }
        Assertions.assertThat(userInfo).isNull()
    }

    @Test
    fun `유저 정보 ID 조회 성공 테스트`() {
        // given
        every { userRepository.findByIdOrNull(user.id!!) } returns user

        // when
        val userInfo = userService.findUserById(user.id!!)

        // then
        verify(exactly = 1) { userRepository.findByIdOrNull(user.id!!) }
        Assertions.assertThat(userInfo).isNotNull
        Assertions.assertThat(user.id).isEqualTo(userInfo!!.id)
    }

    @Test
    fun `유저 정보 ID 조회 불가 테스트`() {
        // given
        val id = UUID.randomUUID()
        every { userRepository.findByIdOrNull(id) } returns null

        // when
        val userInfo = userService.findUserById(id)

        // then
        verify(exactly = 1) { userRepository.findByIdOrNull(id) }
        Assertions.assertThat(userInfo).isNull()
    }

    @Test
    fun `다수 유저 조회 테스트`() {
        // given
        every { userRepository.findAll() } returns listOf(user, githubAdminUser)

        // when
        val users = userService.findUsers()

        // then
        verify(exactly = 1) { userRepository.findAll() }
        Assertions.assertThat(users.size).isEqualTo(2)
    }

    @Test
    fun `다수 어드민 유저 조회 테스트`() {
        // given
        every { userRepository.findUsersByRole(Role.ROLE_ADMIN) } returns listOf(githubAdminUser)

        // when
        val adminUsers = userService.findAdminUsers()

        // then
        verify(exactly = 1) { userRepository.findUsersByRole(Role.ROLE_ADMIN) }
        Assertions.assertThat(adminUsers.size).isEqualTo(1)
    }

    @Test
    fun `유저 수정 ID 조회 불가 실패 테스트`() {
        // given
        val id = UUID.randomUUID()
        every { userRepository.findByIdOrNull(id) } returns null

        // when
        val exception = assertThrows<EntityNotFoundException> {
            userService.modifyUser(
                id,
                UserUpdateRequestDto(
                    "test-url.com",
                    "test",
                    "test1234!",
                    "test1234",
                ),
            )
        }

        // then
        verify(exactly = 1) { userRepository.findByIdOrNull(id) }
        Assertions.assertThat(ErrorCode.NOT_FOUND_ENTITY).isEqualTo(exception.errorCode)
    }

    @Test
    fun `유저 수정 성공 without password 테스트`() {
        // given
        val id = user.id!!
        val userUpdateRequestDto = UserUpdateRequestDto("test-url.com", "test", null, null)
        every { userRepository.findByIdOrNull(id) } returns user

        // when
        val modifyUser = userService.modifyUser(
            id,
            userUpdateRequestDto,
        )

        // then
        verify(exactly = 1) { userRepository.findByIdOrNull(id) }
        Assertions.assertThat("test-url.com").isEqualTo(modifyUser.profileImageUrl)
        Assertions.assertThat("test").isEqualTo(modifyUser.username)
        Assertions.assertThat(user.password).isEqualTo(modifyUser.password)
    }

    @Test
    fun `유저 수정 성공 with password 테스트`() {
        // given
        val id = user.id!!
        val userUpdateRequestDto = UserUpdateRequestDto("test-url.com", "test", "test1234!", "test123!")
        every { userRepository.findByIdOrNull(id) } returns user
        every { passwordEncoder.encode("test123!") } returns "some-encrypted-password"
        every { passwordEncoder.matches(any(), any()) } returns true

        // when
        val modifyUser = userService.modifyUser(
            id,
            userUpdateRequestDto,
        )

        // then
        verify(exactly = 1) { userRepository.findByIdOrNull(id) }
        Assertions.assertThat("test-url.com").isEqualTo(modifyUser.profileImageUrl)
        Assertions.assertThat("test").isEqualTo(modifyUser.username)
        Assertions.assertThat("test123!").isNotEqualTo(modifyUser.password)
    }
}
