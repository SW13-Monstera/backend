package io.csbroker.apiserver.dto.notification

data class NotificationResponseDto(
    val id: Long,
    val content: String,
    val link: Any,
    val isRead: Boolean,
)
