package io.csbroker.apiserver.dto.problem.grade

data class KeywordGradingResponseDto(
    val problem_id: Long,
    val correct_keywords: List<CorrectKeyword>
) {
    data class CorrectKeyword(
        val id: Long,
        val keyword: String,
        val predict_keyword_position: List<Int>,
        val predict_keyword: String
    )
}
