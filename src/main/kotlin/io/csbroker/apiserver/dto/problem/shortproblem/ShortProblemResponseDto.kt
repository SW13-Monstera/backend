package io.csbroker.apiserver.dto.problem.shortproblem

import io.csbroker.apiserver.dto.problem.AdminProblemResponseDto

data class ShortProblemResponseDto(
    val id: Long,
    val title: String,
    val description: String,
    val tags: List<String>,
    val answer: String,
    val score: Double,
    val isActive: Boolean,
    val isGradable: Boolean,
) : AdminProblemResponseDto
