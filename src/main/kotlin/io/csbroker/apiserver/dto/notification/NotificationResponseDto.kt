package io.csbroker.apiserver.dto.notification

import java.time.LocalDateTime

data class NotificationResponseDto(
    val id: Long,
    val content: String,
    val link: String,
    val isRead: Boolean,
    val createdAt: LocalDateTime,
)
