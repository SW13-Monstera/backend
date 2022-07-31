package com.csbroker.apiserver.dto.problem

data class LongProblemSearchResponseDto(
    val problems: List<LongProblemDataDto>,
    val totalPages: Int,
    val totalElements: Long
) {
    data class LongProblemDataDto(
        val id: Long,
        val title: String,
        val creator: String,
        val avgKeywordScore: Double?,
        val avgPromptScore: Double?,
        val userAnswerCnt: Int,
        val isActive: Boolean
    )
}
