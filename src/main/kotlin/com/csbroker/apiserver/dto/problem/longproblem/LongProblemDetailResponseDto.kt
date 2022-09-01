package com.csbroker.apiserver.dto.problem.longproblem

data class LongProblemDetailResponseDto(
    val id: Long,
    val title: String,
    val tags: List<String>,
    val description: String,
    val avgScore: Double?,
    val topScore: Double?,
    val bottomScore: Double?,
    val totalSolved: Int
)