package io.csbroker.apiserver.service.common

import io.csbroker.apiserver.dto.notification.NotificationRequestDto
import io.csbroker.apiserver.model.Notification
import io.csbroker.apiserver.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface NotificationService {
    fun createNotification(notificationRequestDto: NotificationRequestDto): Long
    fun createBulkNotification(notificationRequestListDto: List<NotificationRequestDto>): Int
    fun getNotification(userId: UUID, page: Pageable): Page<Notification>
    fun readNotifications(userId: UUID, notificationIds: List<Long>)
    fun readNotificationById(userId: UUID, id: Long)
    fun getUnreadNotificationCount(userId: UUID): Long
    fun deleteNotifications(user: User, ids: List<Long>)
}
