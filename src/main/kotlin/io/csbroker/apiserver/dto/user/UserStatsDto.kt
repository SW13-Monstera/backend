package io.csbroker.apiserver.dto.user

import io.csbroker.apiserver.model.GradingHistory

data class UserStatsDto(
    val correctAnsweredProblem: List<ProblemStatsDto>,
    val wrongAnsweredProblem: List<ProblemStatsDto>,
    val partialAnsweredProblem: List<ProblemStatsDto>,
    val count: Map<String, Int>,
    val rank: Long?,
    val score: Double,
) {
    constructor() : this(arrayListOf(), arrayListOf(), arrayListOf(), mapOf(), null, 0.0)
    data class ProblemStatsDto(
        val id: Long,
        val type: String,
        val title: String,
    ) {
        constructor() : this(0, "", "")
        companion object {
            fun from(problemId: Long, gradingHistory: GradingHistory) = ProblemStatsDto(
                problemId,
                gradingHistory.problem.dtype,
                gradingHistory.problem.title,
            )
        }
    }
}
