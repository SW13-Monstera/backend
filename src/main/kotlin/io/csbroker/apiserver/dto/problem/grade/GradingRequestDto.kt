package io.csbroker.apiserver.dto.problem.grade

import io.csbroker.apiserver.common.enums.GradingStandardType
import io.csbroker.apiserver.model.LongProblem

data class GradingRequestDto(
    val problem_id: Long,
    val user_answer: String,
    val keyword_standards: List<GradingKeyword>,
    val content_standards: List<GradingContent>
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
                },
                problem.gradingStandards.filter {
                    it.type == GradingStandardType.CONTENT
                }.map {
                    GradingContent(
                        it.id!!,
                        it.content
                    )
                }
            )
        }
    }
}
