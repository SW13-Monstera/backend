package io.csbroker.apiserver.dto.problem.problemset

import io.csbroker.apiserver.dto.problem.ProblemResponseDto

data class ProblemSetDetailResponseDto(
    val id: Long,
    val problems: List<ProblemResponseDto>,
    val name: String,
    val description: String
)
