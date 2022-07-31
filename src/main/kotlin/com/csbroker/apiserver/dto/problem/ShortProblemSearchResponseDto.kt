package com.csbroker.apiserver.dto.problem

data class ShortProblemSearchResponseDto(
    val problems: List<ShortProblemDataDto>,
    val totalPages: Int,
    val totalElements: Long
) {
    data class ShortProblemDataDto(
        val id: Long,
        val title: String,
        val creator: String,
        val answerRate: Double?,
        val userAnswerCnt: Int,
        val isActive: Boolean
    )
}
