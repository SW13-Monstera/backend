package io.csbroker.apiserver.dto.user

import io.csbroker.apiserver.model.GradingHistory

data class UserStatsDto(
    val correctAnsweredProblem: List<ProblemStatsDto>,
    val wrongAnsweredProblem: List<ProblemStatsDto>,
    val partialAnsweredProblem: List<ProblemStatsDto>,
    val count: Map<String, Int>,
    val rank: Long?,
    val score: Double
) {
    data class ProblemStatsDto(
        val id: Long,
        val type: String,
        val title: String
    ) {
        companion object {
            fun from(problemId: Long, gradingHistory: GradingHistory) = ProblemStatsDto(
                problemId,
                gradingHistory.problem.dtype,
                gradingHistory.problem.title
            )
        }
    }
}
