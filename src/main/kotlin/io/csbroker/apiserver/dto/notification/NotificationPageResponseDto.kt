package io.csbroker.apiserver.dto.notification

import io.csbroker.apiserver.model.Notification
import org.springframework.data.domain.Page

data class NotificationPageResponseDto(
    val contents: List<NotificationResponseDto>,
    val currentPage: Int,
    val totalPages: Int,
    val totalElements: Long,
    val numberOfElements: Int,
    val size: Int
) {
    constructor(pageData: Page<Notification>) : this(
        pageData.content.map { it.toNotificationResponseDto() },
        pageData.pageable.pageNumber,
        pageData.totalPages,
        pageData.totalElements,
        pageData.numberOfElements,
        pageData.size
    )
}
