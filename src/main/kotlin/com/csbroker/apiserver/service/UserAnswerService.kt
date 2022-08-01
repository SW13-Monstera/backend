package com.csbroker.apiserver.service

import com.csbroker.apiserver.dto.UserAnswerResponseDto
import com.csbroker.apiserver.dto.UserAnswerSearchResponseDto
import com.csbroker.apiserver.dto.UserAnswerUpsertDto
import org.springframework.data.domain.Pageable

interface UserAnswerService {
    fun createUserAnswers(userAnswers: List<UserAnswerUpsertDto>): Int
    fun createUserAnswer(userAnswer: UserAnswerUpsertDto): Long
    fun findUserAnswerById(id: Long): UserAnswerResponseDto
    fun labelUserAnswer(email: String, userAnswerId: Long, selectedGradingStandardIds: List<Long>): Long
    fun validateUserAnswer(email: String, userAnswerId: Long, selectedGradingStandardIds: List<Long>): Long

    fun findUserAnswersByQuery(
        id: Long?,
        assignedBy: String?,
        validatedBy: String?,
        problemTitle: String?,
        answer: String?,
        isLabeled: Boolean?,
        isValidated: Boolean?,
        pageable: Pageable
    ): UserAnswerSearchResponseDto
}
