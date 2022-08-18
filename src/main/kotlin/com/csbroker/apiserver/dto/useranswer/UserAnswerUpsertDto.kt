package com.csbroker.apiserver.dto.useranswer

import java.util.UUID

data class UserAnswerUpsertDto(
    val assignedUserId: UUID?,
    val validatingUserId: UUID?,
    val answer: String,
    val problemId: Long
)
