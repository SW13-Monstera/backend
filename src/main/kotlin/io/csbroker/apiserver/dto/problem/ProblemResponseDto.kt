package io.csbroker.apiserver.dto.problem

data class ProblemResponseDto(
    val id: Long,
    val title: String,
    val tags: List<String>,
    val avgScore: Double?,
    val totalSubmission: Int,
    val type: String,
)
