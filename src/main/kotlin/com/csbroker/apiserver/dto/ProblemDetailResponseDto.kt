package com.csbroker.apiserver.dto

import java.util.UUID

data class ProblemDetailResponseDto(
    val id: UUID,
    val title: String,
    val tags: List<String>,
    val description: String,
    val avgScore: Double?,
    val topScore: Float?,
    val bottomScore: Float?,
    val totalSolved: Int
)
