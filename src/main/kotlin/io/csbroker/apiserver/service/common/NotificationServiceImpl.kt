package io.csbroker.apiserver.service.common

import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.dto.notification.NotificationRequestDto
import io.csbroker.apiserver.model.Notification
import io.csbroker.apiserver.model.User
import io.csbroker.apiserver.repository.common.NotificationRepository
import io.csbroker.apiserver.repository.user.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Transactional(readOnly = true)
@Service
class NotificationServiceImpl(
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository,
) : NotificationService {
    @Transactional
    override fun createNotification(notificationRequestDto: NotificationRequestDto): Long {
        val findUser = userRepository.findByIdOrNull(notificationRequestDto.userId)
            ?: throw EntityNotFoundException("${notificationRequestDto.userId} 에 해당하는 유저를 찾을 수 없습니다.")

        val notification = Notification(notificationRequestDto, findUser)

        return notificationRepository.save(notification).id!!
    }

    @Transactional
    override fun createBulkNotification(notificationRequestListDto: List<NotificationRequestDto>): Int {
        val requestUserIds = notificationRequestListDto.map { it.userId }.toSet()
        val users = userRepository.findAllById(requestUserIds)

        if (users.size != requestUserIds.size) {
            throw EntityNotFoundException("존재하지 않는 유저가 요청에 포함되어 있습니다.")
        }

        return notificationRepository.saveAll(
            notificationRequestListDto.map { Notification(it, users.find { u -> u.id == it.userId }!!) },
        ).size
    }

    override fun getNotification(userId: UUID, page: Pageable): Page<Notification> {
        return notificationRepository.findByUserId(userId, page)
    }

    @Transactional
    override fun readNotifications(userId: UUID, notificationIds: List<Long>) {
        val updatedCount = notificationRepository.setIsReadByIdIn(userId, notificationIds)

        if (updatedCount != notificationIds.size) {
            throw EntityNotFoundException("해당하는 알림을 찾을 수 없습니다.")
        }
    }

    @Transactional
    override fun readNotificationById(userId: UUID, id: Long) {
        val updatedCount = notificationRepository.setIsReadById(userId, id)
        if (updatedCount == 0) {
            throw EntityNotFoundException("해당하는 알림을 찾을 수 없습니다.")
        }
    }

    override fun getUnreadNotificationCount(userId: UUID): Long {
        return notificationRepository.countUnReadByUserId(userId)
    }

    @Transactional
    override fun deleteNotifications(user: User, ids: List<Long>) {
        val deletedCnt = notificationRepository.deleteAllByUserAndIdIn(user, ids)

        if (deletedCnt != ids.size) {
            throw EntityNotFoundException("해당하는 알림을 찾을 수 없습니다.")
        }
    }
}
