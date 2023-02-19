package io.csbroker.apiserver.dto.problem.grade

import com.fasterxml.jackson.annotation.JsonProperty

data class KeywordGradingResponseDto(
    @field:JsonProperty("problem_id")
    val problemId: Long,
    @field:JsonProperty("keyword_standards")
    val correctKeywords: List<CorrectKeyword>,
) {
    data class CorrectKeyword(
        val id: Long,
        val keyword: String,
        @field:JsonProperty("predict_keyword_position")
        val predictKeywordPosition: List<Int>,
        @field:JsonProperty("predict_keyword")
        val predictKeyword: String,
    )
}
