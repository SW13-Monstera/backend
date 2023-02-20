package io.csbroker.apiserver.dto.problem.grade

import com.fasterxml.jackson.annotation.JsonProperty

data class GradingResponseDto(
    @field:JsonProperty("problem_id")
    val problemId: Long,
    @field:JsonProperty("correct_keywords")
    val correctKeywords: List<CorrectKeyword>,
    @field:JsonProperty("correct_contents")
    val correctContents: List<CorrectContent>,
) {
    data class CorrectKeyword(
        val id: Long,
        val keyword: String,
        @field:JsonProperty("predict_keyword_position")
        val predictKeywordPosition: List<Int>,
        @field:JsonProperty("predict_keyword")
        val predictKeyword: String,
    )

    data class CorrectContent(
        val id: Long,
        val content: String,
    )
}
