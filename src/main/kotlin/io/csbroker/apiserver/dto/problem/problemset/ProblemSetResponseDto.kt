package io.csbroker.apiserver.dto.problem.problemset

data class ProblemSetResponseDto(
    val id: Long,
    val problemCnt: Int,
    val name: String,
    val description: String,
)
