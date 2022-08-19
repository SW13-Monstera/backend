package com.csbroker.apiserver.dto.problem.grade

import com.csbroker.apiserver.common.enums.GradingStandardType
import com.csbroker.apiserver.model.LongProblem

data class GradingRequestDto(
    val problem_id: Long,
    val user_answer: String,
    val keywords: List<GradingKeyword>
) {
    data class GradingKeyword(
        val id: Long,
        val content: String
    )

    companion object {
        fun createGradingRequestDto(problem: LongProblem, answer: String): GradingRequestDto {
            return GradingRequestDto(
                problem.id!!,
                answer,
                problem.gradingStandards.filter {
                    it.type == GradingStandardType.KEYWORD
                }.map {
                    GradingKeyword(
                        it.id!!,
                        it.content
                    )
                }
            )
        }
    }
}
