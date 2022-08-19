package com.csbroker.apiserver.dto.useranswer

data class UserAnswerBatchInsertDto(
    val size: Int,
    val userAnswers: List<UserAnswerUpsertDto>
)
