package com.csbroker.apiserver.service

import com.csbroker.apiserver.dto.UserAnswerResponseDto
import com.csbroker.apiserver.dto.UserAnswerUpsertDto

interface UserAnswerService {
    fun createUserAnswers(userAnswers: List<UserAnswerUpsertDto>): Int
    fun createUserAnswer(userAnswer: UserAnswerUpsertDto): Long
    fun findUserAnswerById(id: Long) : UserAnswerResponseDto
    fun labelUserAnswer(userAnswerId: Long, selectedGradingStandardIds: List<Long>) : Long
}
