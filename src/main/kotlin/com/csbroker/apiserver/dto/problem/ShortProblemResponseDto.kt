package com.csbroker.apiserver.dto.problem

data class ShortProblemResponseDto(
    val id: Long,
    val title: String,
    val description: String,
    val tags: List<String>,
    val answer: String,
    val score: Double,
    val isActive: Boolean,
    val isGradable: Boolean
)
