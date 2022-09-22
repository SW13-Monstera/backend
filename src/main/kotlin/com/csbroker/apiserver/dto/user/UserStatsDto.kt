package com.csbroker.apiserver.dto.user

data class UserStatsDto(
    val correctAnsweredProblem: List<ProblemStatsDto>,
    val wrongAnsweredProblem: List<ProblemStatsDto>,
    val partialAnsweredProblem: List<ProblemStatsDto>,
    val count: Map<String, Int>,
    val rank: Long,
    val score: Double
) {
    data class ProblemStatsDto(
        val id: Long,
        val type: String,
        val title: String
    )
}
