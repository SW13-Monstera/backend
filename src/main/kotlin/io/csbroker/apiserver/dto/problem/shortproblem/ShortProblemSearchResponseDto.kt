package io.csbroker.apiserver.dto.problem.shortproblem

import io.csbroker.apiserver.dto.problem.AdminProblemSearchResponseDto

data class ShortProblemSearchResponseDto(
    val problems: List<ShortProblemDataDto>,
    val totalPages: Int,
    val totalElements: Long,
) : AdminProblemSearchResponseDto {
    data class ShortProblemDataDto(
        val id: Long,
        val title: String,
        val creator: String,
        val answerRate: Double?,
        val userAnswerCnt: Int,
        val isActive: Boolean,
    )
}
