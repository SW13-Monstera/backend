package io.csbroker.apiserver.dto.useranswer

import java.util.UUID

data class AssignUserAnswerDto(
    val userAnswerIds: List<Long> = listOf(),
    val assigneeId: UUID
)
