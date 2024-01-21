package io.csbroker.apiserver.dto.problem.longproblem

data class LongProblemResponseDto(
    val id: Long,
    val title: String,
    val description: String,
    val standardAnswers: List<String>,
    val tags: List<String>,
    val isActive: Boolean,
)
