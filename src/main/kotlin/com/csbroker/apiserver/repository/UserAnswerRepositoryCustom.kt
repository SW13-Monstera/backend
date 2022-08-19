package com.csbroker.apiserver.repository

import com.csbroker.apiserver.dto.useranswer.UserAnswerUpsertDto

interface UserAnswerRepositoryCustom {
    fun batchInsert(userAnswers: List<UserAnswerUpsertDto>)
}
