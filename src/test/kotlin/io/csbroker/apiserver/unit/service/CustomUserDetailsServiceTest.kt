package io.csbroker.apiserver.unit.service

import io.csbroker.apiserver.auth.ProviderType
import io.csbroker.apiserver.common.enums.ErrorCode
import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.model.User
import io.csbroker.apiserver.repository.UserRepository
import io.csbroker.apiserver.service.CustomUserDetailsService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class CustomUserDetailsServiceTest {
    private val userRepository: UserRepository = mockk()
    private val customUserDetailsService: CustomUserDetailsService = CustomUserDetailsService(userRepository)

    private val user: User = User(
        id = UUID.randomUUID(),
        email = "test@test.com",
        password = "test1234!",
        username = "test",
        providerType = ProviderType.LOCAL
    )

    @Test
    fun `Spring Security 유저 정보 이메일 조회 성공 테스트`() {
        // given
        every { userRepository.findByEmail("test@test.com") } returns user

        // when
        val userDetails = customUserDetailsService.loadUserByUsername("test@test.com")

        // then
        verify(exactly = 1) { userRepository.findByEmail("test@test.com") }
        Assertions.assertThat(user.id.toString()).isEqualTo(userDetails.username)
        Assertions.assertThat(user.password).isEqualTo(userDetails.password)
    }

    @Test
    fun `Spring Security 유저 정보 이메일 조회 불가 테스트`() {
        // given
        every { userRepository.findByEmail("test@test.com") } returns null

        // when
        val exception = assertThrows<EntityNotFoundException> {
            customUserDetailsService.loadUserByUsername("test@test.com")
        }

        // then
        verify(exactly = 1) { userRepository.findByEmail("test@test.com") }
        Assertions.assertThat(ErrorCode.NOT_FOUND_ENTITY).isEqualTo(exception.errorCode)
    }
}
