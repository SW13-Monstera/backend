package io.csbroker.apiserver.dto.problem.longproblem

import io.csbroker.apiserver.dto.problem.AdminProblemSearchResponseDto

data class LongProblemSearchResponseDto(
    val problems: List<LongProblemDataDto>,
    val totalPages: Int,
    val totalElements: Long,
) : AdminProblemSearchResponseDto {
    data class LongProblemDataDto(
        val id: Long,
        val title: String,
        val creator: String,
        val avgKeywordScore: Double?,
        val avgContentScore: Double?,
        val userAnswerCnt: Int,
        val isActive: Boolean,
    )
}
