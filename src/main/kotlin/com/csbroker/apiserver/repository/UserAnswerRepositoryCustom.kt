package com.csbroker.apiserver.repository

import com.csbroker.apiserver.dto.UserAnswerUpsertDto

interface UserAnswerRepositoryCustom {
    fun batchInsert(userAnswers: List<UserAnswerUpsertDto>)
}
