package io.csbroker.apiserver.dto.notification

import java.util.UUID

data class NotificationRequestDto(
    val content: String,
    val userId: UUID,
    val link: String,
)
