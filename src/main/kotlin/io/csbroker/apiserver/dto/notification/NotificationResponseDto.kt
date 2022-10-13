package io.csbroker.apiserver.dto.notification

data class NotificationResponseDto(
    val id: Long,
    val content: String,
    val link: String,
    val isRead: Boolean,
)
