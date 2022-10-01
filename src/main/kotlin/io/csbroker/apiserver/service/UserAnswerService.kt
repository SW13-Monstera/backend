package io.csbroker.apiserver.service

import io.csbroker.apiserver.dto.useranswer.UserAnswerResponseDto
import io.csbroker.apiserver.dto.useranswer.UserAnswerSearchResponseDto
import io.csbroker.apiserver.dto.useranswer.UserAnswerUpsertDto
import org.springframework.data.domain.Pageable
import java.util.UUID

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

    fun assignLabelUserAnswer(
        userAnswerIds: List<Long>,
        userId: UUID
    )

    fun assignValidationUserAnswer(
        userAnswerIds: List<Long>,
        userId: UUID
    )
}
