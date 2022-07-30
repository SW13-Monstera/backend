package com.csbroker.apiserver.dto

import com.csbroker.apiserver.common.enums.GradingStandardType
import com.csbroker.apiserver.dto.user.GradingStandardResponseDto
import com.csbroker.apiserver.model.UserAnswer

data class UserAnswerResponseDto(
    val id: Long,
    val answer: String,
    val isLabeled: Boolean,
    val isValidated: Boolean,
    val keywordsGradingStandards: List<GradingStandardResponseDto>,
    val promptGradingStandards: List<GradingStandardResponseDto>,
    val selectedGradingStands: List<Long>
) {
    companion object {
        fun fromUserAnswer(userAnswer: UserAnswer): UserAnswerResponseDto {
            val keywords = userAnswer.problem
                .gradingStandards.filter { it.type == GradingStandardType.KEYWORD }
                .map { GradingStandardResponseDto.fromGradingStandard(it) }

            val prompts = userAnswer.problem
                .gradingStandards.filter { it.type == GradingStandardType.PROMPT }
                .map { GradingStandardResponseDto.fromGradingStandard(it) }

            val selectedGradingStandards = userAnswer.userAnswerGradingStandards.map {
                it.gradingStandard.id!!
            }

            return UserAnswerResponseDto(
                userAnswer.id!!,
                userAnswer.answer,
                userAnswer.isLabeled,
                userAnswer.isValidated,
                keywords,
                prompts,
                selectedGradingStandards,
            )
        }
    }
}
