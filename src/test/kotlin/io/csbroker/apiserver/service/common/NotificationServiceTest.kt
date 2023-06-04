package io.csbroker.apiserver.service.common

import io.csbroker.apiserver.auth.ProviderType
import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.dto.notification.NotificationRequestDto
import io.csbroker.apiserver.model.User
import io.csbroker.apiserver.repository.common.NotificationRepository
import io.csbroker.apiserver.repository.user.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageRequest
import java.util.UUID

class NotificationServiceTest {
    private val notificationRepository = mockk<NotificationRepository>()
    private val userRepository = mockk<UserRepository>()
    private lateinit var service: NotificationService
    private val user = User(
        id = UUID.randomUUID(),
        email = "test@test.com",
        password = "test1234!",
        username = "test",
        providerType = ProviderType.LOCAL,
    )

    @BeforeEach
    fun setUp() {
        service = NotificationServiceImpl(
            notificationRepository,
            userRepository,
        )
    }

    @Test
    fun `createNotification - 존재하지 않는 유저가 알림을 생성할 시 예외가 발생합니다`() {
        // given
        val req = NotificationRequestDto(content = "content", userId = user.id!!, link = "link")
        every { userRepository.findByIdOrNull(req.userId) } returns null

        // when & then
        assertThrows<EntityNotFoundException> { service.createNotification(req) }
        verify { userRepository.findByIdOrNull(req.userId) }
    }

    @Test
    fun `getNotification - 존재하지 않는 유저가 알림을 조회할 시 예외가 발생합니다`() {
        // given
        val email = "notExist@email.com"
        val page = PageRequest.of(0, 10)
        every { userRepository.findByEmail(email) } returns null

        // when & then
        assertThrows<EntityNotFoundException> { service.getNotification(email, page) }
        verify { userRepository.findByEmail(email) }
    }

    @Test
    fun `readNotifications - 존재하지 않는 유저가 알림을 읽을 시 예외가 발생합니다`() {
        // given
        val email = "notExist@email.com"
        val notificationIds = listOf(1L, 2L)
        every { userRepository.findByEmail(email) } returns null

        // when & then
        assertThrows<EntityNotFoundException> { service.readNotifications(email, notificationIds) }
        verify { userRepository.findByEmail(email) }
    }

    @Test
    fun `readNotifications - 존재하지 않는 알림을 읽을 시 예외가 발생합니다`() {
        // given
        val email = user.email
        val notificationIds = listOf(1L, 2L)
        every { userRepository.findByEmail(email) } returns user
        every { notificationRepository.setIsReadByIdIn(user.id!!, notificationIds) } returns 0

        // when & then
        assertThrows<EntityNotFoundException> { service.readNotifications(email, notificationIds) }
        verify { userRepository.findByEmail(email) }
        verify { notificationRepository.setIsReadByIdIn(user.id!!, notificationIds) }
    }

    @Test
    fun `readNotificationById - 존재하지 않는 유저가 알림을 읽을 시 예외가 발생합니다`() {
        // given
        val email = "notExist@email.com"
        val notificationId = 1L
        every { userRepository.findByEmail(email) } returns null

        // when & then
        assertThrows<EntityNotFoundException> { service.readNotificationById(email, notificationId) }
        verify { userRepository.findByEmail(email) }
    }

    @Test
    fun `readNotificationById - 존재하지 않는 알림을 읽을 시 예외가 발생합니다`() {
        // given
        val email = user.email
        val notificationId = 1L
        every { userRepository.findByEmail(email) } returns user
        every { notificationRepository.setIsReadById(user.id!!, notificationId) } returns 0

        // when & then
        assertThrows<EntityNotFoundException> { service.readNotificationById(email, notificationId) }
        verify { userRepository.findByEmail(email) }
        verify { notificationRepository.setIsReadById(user.id!!, notificationId) }
    }

    @Test
    fun `getUnreadNotificationCount - 존재하지 않는 유저가 요청시 예외가 발생합니다`() {
        // given
        val email = "notExist@email.com"
        every { userRepository.findByEmail(email) } returns null

        // when & then
        assertThrows<EntityNotFoundException> { service.getUnreadNotificationCount(email) }
        verify { userRepository.findByEmail(email) }
    }

    @Test
    fun `deleteNotifications - 존재하지 않는 유저가 요청시 예외가 발생합니다`() {
        // given
        val email = "notExist@email.com"
        val notificationIds = listOf(1L, 2L)
        every { userRepository.findByEmail(email) } returns null

        // when & then
        assertThrows<EntityNotFoundException> { service.deleteNotifications(email, notificationIds) }
        verify { userRepository.findByEmail(email) }
    }

    @Test
    fun `deleteNotifications - 존재하지 않는 알림에 대한 삭제 요청시 예외가 발생합니다`() {
        // given
        val email = user.email
        val notificationIds = listOf(1L, 2L)
        every { userRepository.findByEmail(email) } returns user
        every { notificationRepository.deleteAllByUserAndIdIn(user, notificationIds) } returns notificationIds.size - 1

        // when & then
        assertThrows<EntityNotFoundException> { service.deleteNotifications(email, notificationIds) }
        verify { userRepository.findByEmail(email) }
        verify { notificationRepository.deleteAllByUserAndIdIn(user, notificationIds) }
    }
}
