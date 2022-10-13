package io.csbroker.apiserver.dto.notification

data class NotificationBulkInsertDto(
    val content: List<NotificationRequestDto>
)
