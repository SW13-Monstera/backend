package com.csbroker.apiserver.dto

import java.util.UUID

data class ProblemResponseDto(
    val id: UUID,
    val title: String,
    val tags: List<String>,
    val avgScore: Double?,
    val totalSolved: Int
)
