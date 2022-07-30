package com.csbroker.apiserver.service

import com.csbroker.apiserver.dto.UserAnswerUpsertDto

interface UserAnswerService {
    fun createUserAnswers(userAnswers: List<UserAnswerUpsertDto>): Int
}
