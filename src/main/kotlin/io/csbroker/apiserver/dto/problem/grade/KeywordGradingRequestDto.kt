package io.csbroker.apiserver.dto.problem.grade

import io.csbroker.apiserver.common.enums.GradingStandardType
import io.csbroker.apiserver.model.LongProblem

data class KeywordGradingRequestDto(
    val problem_id: Long,
    val user_answer: String,
    val keyword_standards: List<GradingKeyword>
) {
    data class GradingContent(
        val id: Long,
        val content: String
    )

    data class GradingKeyword(
        val id: Long,
        val content: String
    )

    companion object {
        fun createKeywordGradingRequestDto(problem: LongProblem, answer: String): KeywordGradingRequestDto {
            return KeywordGradingRequestDto(
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
