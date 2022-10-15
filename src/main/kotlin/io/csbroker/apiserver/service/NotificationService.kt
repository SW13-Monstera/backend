package io.csbroker.apiserver.service

import io.csbroker.apiserver.dto.notification.NotificationRequestDto
import io.csbroker.apiserver.model.Notification
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface NotificationService {
    fun createNotification(notificationRequestDto: NotificationRequestDto): Long
    fun createBulkNotification(notificationRequestListDto: List<NotificationRequestDto>): Int
    fun getNotification(email: String, page: Pageable): Page<Notification>
    fun readNotifications(email: String, notificationIds: List<Long>)
    fun readNotificationById(email: String, id: Long)
    fun getUnreadNotificationCount(email: String): Long
}
