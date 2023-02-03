package io.csbroker.apiserver.dto.problem.problemset

data class ProblemSetUpsertRequestDto(
    val problemIds: List<Long>,
    val name: String,
    val description: String,
)
