package io.csbroker.apiserver.dto.problem.grade

import com.fasterxml.jackson.annotation.JsonProperty
import io.csbroker.apiserver.model.LongProblem

data class KeywordGradingRequestDto(
    @field:JsonProperty("problem_id")
    val problemId: Long,
    @field:JsonProperty("user_answer")
    val userAnswer: String,
    @field:JsonProperty("keyword_standards")
    val keywordStandards: List<GradingKeyword>,
) {
    data class GradingKeyword(
        val id: Long,
        val content: String,
    )

    companion object {
        fun createKeywordGradingRequestDto(problem: LongProblem, answer: String): KeywordGradingRequestDto {
            return KeywordGradingRequestDto(
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
            )
        }
    }
}
