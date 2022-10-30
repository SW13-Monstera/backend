package io.csbroker.apiserver.repository

import io.csbroker.apiserver.dto.notification.NotificationRequestDto

interface NotificationRepositoryCustom {
    fun insertBulkNotifications(notifications: List<NotificationRequestDto>)
}
