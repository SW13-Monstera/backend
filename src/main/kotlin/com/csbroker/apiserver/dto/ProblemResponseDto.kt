package com.csbroker.apiserver.dto

data class ProblemResponseDto(
    val id: Long,
    val title: String,
    val tags: List<String>,
    val avgScore: Double?,
    val totalSolved: Int
)
