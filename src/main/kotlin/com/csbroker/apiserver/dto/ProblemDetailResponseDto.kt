package com.csbroker.apiserver.dto

data class ProblemDetailResponseDto(
    val title: String,
    val tags: List<String>,
    val description: String,
    val avgScore: Double,
    val topScore: Float,
    val bottomScore: Float,
    val totalSolved: Int
)
