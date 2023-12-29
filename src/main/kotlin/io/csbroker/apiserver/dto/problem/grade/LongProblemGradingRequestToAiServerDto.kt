package io.csbroker.apiserver.dto.problem.grade

import com.fasterxml.jackson.annotation.JsonProperty
import io.csbroker.apiserver.model.LongProblem

data class LongProblemGradingRequestToAiServerDto(
    @field:JsonProperty("problem_id")
    val problemId: Long,
    @field:JsonProperty("user_answer")
    val userAnswer: String,
    @field:JsonProperty("keyword_standards")
    val keywordStandards: List<GradingKeyword>,
    @field:JsonProperty("content_standards")
    val contentStandards: List<GradingContent>,
) {
    data class GradingContent(
        val id: Long,
        val content: String,
    )

    data class GradingKeyword(
        val id: Long,
        val content: String,
    )

    companion object {
        fun createGradingRequestDto(problem: LongProblem, answer: String): LongProblemGradingRequestToAiServerDto {
            return LongProblemGradingRequestToAiServerDto(
                problem.id,
                answer,
                problem.gradingStandards.filter {
                    it.type == GradingStandardType.KEYWORD
                }.map {
                    GradingKeyword(
                        it.id,
                        it.content,
                    )
                },
                problem.gradingStandards.filter {
                    it.type == GradingStandardType.CONTENT
                }.map {
                    GradingContent(
                        it.id,
                        it.content,
                    )
                },
            )
        }
    }
}
