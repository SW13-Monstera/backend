package io.csbroker.apiserver.dto.problem.problemset

import io.csbroker.apiserver.model.ProblemSet

data class ProblemSetUpsertRequestDto(
    val problemIds: List<Long>,
    val name: String,
    val description: String,
) {
    fun toProblemSet(): ProblemSet {
        return ProblemSet(name = name, description = description)
    }
}
