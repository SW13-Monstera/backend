package com.csbroker.apiserver.dto

data class UserAnswerBatchInsertDto(
    val size: Int,
    val userAnswers: List<UserAnswerUpsertDto>
)
