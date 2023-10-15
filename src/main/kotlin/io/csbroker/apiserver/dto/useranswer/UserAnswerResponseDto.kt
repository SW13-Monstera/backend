package io.csbroker.apiserver.dto.useranswer

import io.csbroker.apiserver.common.enums.GradingStandardType
import io.csbroker.apiserver.dto.user.GradingStandardResponseDto
import io.csbroker.apiserver.model.UserAnswer

data class UserAnswerResponseDto(
    val id: Long,
    val problemId: Long,
    val problemTitle: String,
    val problemDescription: String,
    val answer: String,
    val isLabeled: Boolean,
    val isValidated: Boolean,
    val keywordsGradingStandards: List<GradingStandardResponseDto>,
    val contentGradingStandards: List<GradingStandardResponseDto>,
    val selectedGradingStandards: List<Long>,
) {
    companion object {
        fun fromUserAnswer(userAnswer: UserAnswer): UserAnswerResponseDto {
            val keywords = userAnswer.problem
                .gradingStandards.filter { it.type == GradingStandardType.KEYWORD }
                .map { GradingStandardResponseDto.fromGradingStandard(it) }

            val contents = userAnswer.problem
                .gradingStandards.filter { it.type == GradingStandardType.CONTENT }
                .map { GradingStandardResponseDto.fromGradingStandard(it) }

            val selectedGradingStandards = userAnswer.userAnswerGradingStandards.map {
                it.gradingStandard.id
            }

            return UserAnswerResponseDto(
                userAnswer.id,
                userAnswer.problem.id,
                userAnswer.problem.title,
                userAnswer.problem.description,
                userAnswer.answer,
                userAnswer.isLabeled,
                userAnswer.isValidated,
                keywords,
                contents,
                selectedGradingStandards,
            )
        }
    }
}
