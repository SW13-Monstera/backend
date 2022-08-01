package com.csbroker.apiserver.dto

import java.time.LocalDateTime

data class UserAnswerSearchResponseDto(
    val userAnswers: List<UserAnswerDataDto>,
    val totalPages: Int,
    val totalElements: Long
) {
    data class UserAnswerDataDto(
        val id: Long,
        val problemTitle: String,
        val assignedUsername: String?,
        val validatingUsername: String?,
        val updatedAt: LocalDateTime,
        val isLabeled: Boolean,
        val isValidated: Boolean
    )
}
