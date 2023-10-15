package io.csbroker.apiserver.service.common

import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.dto.notification.NotificationRequestDto
import io.csbroker.apiserver.model.Notification
import io.csbroker.apiserver.repository.common.NotificationRepository
import io.csbroker.apiserver.repository.user.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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

        return notificationRepository.save(notification).id
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

    override fun getNotification(email: String, page: Pageable): Page<Notification> {
        val findUser = userRepository.findByEmail(email)
            ?: throw EntityNotFoundException("$email 에 해당하는 유저를 찾을 수 없습니다.")

        return notificationRepository.findByUserId(findUser.id!!, page)
    }

    @Transactional
    override fun readNotifications(email: String, notificationIds: List<Long>) {
        val findUser = userRepository.findByEmail(email)
            ?: throw EntityNotFoundException("$email 에 해당하는 유저를 찾을 수 없습니다.")

        val updatedCount = notificationRepository.setIsReadByIdIn(findUser.id!!, notificationIds)

        if (updatedCount != notificationIds.size) {
            throw EntityNotFoundException("해당하는 알림을 찾을 수 없습니다.")
        }
    }

    @Transactional
    override fun readNotificationById(email: String, id: Long) {
        val findUser = userRepository.findByEmail(email)
            ?: throw EntityNotFoundException("$email 에 해당하는 유저를 찾을 수 없습니다.")

        val updatedCount = notificationRepository.setIsReadById(findUser.id!!, id)

        if (updatedCount == 0) {
            throw EntityNotFoundException("해당하는 알림을 찾을 수 없습니다.")
        }
    }

    override fun getUnreadNotificationCount(email: String): Long {
        val findUser = userRepository.findByEmail(email)
            ?: throw EntityNotFoundException("$email 에 해당하는 유저를 찾을 수 없습니다.")

        return notificationRepository.countUnReadByUserId(findUser.id!!)
    }

    @Transactional
    override fun deleteNotifications(email: String, ids: List<Long>) {
        val findUser = userRepository.findByEmail(email)
            ?: throw EntityNotFoundException("$email 에 해당하는 유저를 찾을 수 없습니다.")

        val deletedCnt = notificationRepository.deleteAllByUserAndIdIn(findUser, ids)

        if (deletedCnt != ids.size) {
            throw EntityNotFoundException("해당하는 알림을 찾을 수 없습니다.")
        }
    }
}
