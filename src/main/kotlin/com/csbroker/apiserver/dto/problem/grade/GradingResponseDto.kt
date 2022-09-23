package com.csbroker.apiserver.dto.problem.grade

data class GradingResponseDto(
    val problem_id: Long,
    val correct_keywords: List<CorrectKeyword>,
    val correct_prompt_ids: List<Long>
) {
    data class CorrectKeyword(
        val id: Long,
        val keyword: String,
        val predict_keyword_position: List<Int>,
        val predict_keyword: String
    )
}
