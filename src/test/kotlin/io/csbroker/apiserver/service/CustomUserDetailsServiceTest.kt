package io.csbroker.apiserver.service

import io.csbroker.apiserver.auth.ProviderType
import io.csbroker.apiserver.common.enums.ErrorCode
import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.model.User
import io.csbroker.apiserver.repository.user.UserRepository
import io.csbroker.apiserver.service.auth.CustomUserDetailsService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.core.userdetails.UserDetailsService
import java.util.UUID

class CustomUserDetailsServiceTest {
    private lateinit var userRepository: UserRepository
    private lateinit var sut: UserDetailsService

    @BeforeEach
    fun setUp() {
        userRepository = mockk()
        sut = CustomUserDetailsService(userRepository)
    }

    @Test
    fun `Spring Security 유저 정보 이메일 조회 성공 테스트`() {
        // given
        val user = createUser()
        every { userRepository.findByEmail(any()) } returns user

        // when
        val userDetails = sut.loadUserByUsername("test@test.com")

        // then
        verify(exactly = 1) { userRepository.findByEmail(any()) }
        assertThat(user.id.toString()).isEqualTo(userDetails.username)
        assertThat(user.password).isEqualTo(userDetails.password)
    }

    @Test
    fun `Spring Security 유저 정보 이메일 조회 불가 테스트`() {
        // given
        every { userRepository.findByEmail(any()) } returns null

        // when
        val exception = assertThrows<EntityNotFoundException> {
            sut.loadUserByUsername("test@test.com")
        }

        // then
        verify(exactly = 1) { userRepository.findByEmail(any()) }
        assertThat(ErrorCode.NOT_FOUND_ENTITY).isEqualTo(exception.errorCode)
    }

    private fun createUser() = User(
        id = UUID.randomUUID(),
        email = "test@test.com",
        password = "test1234!",
        username = "test",
        providerType = ProviderType.LOCAL,
    )
}
