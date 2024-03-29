package io.csbroker.apiserver.dto.problem.longproblem

import io.csbroker.apiserver.dto.user.GradingStandardResponseDto

data class LongProblemResponseDto(
    val id: Long,
    val title: String,
    val description: String,
    val standardAnswers: List<String>,
    val tags: List<String>,
    val gradingStandards: List<GradingStandardResponseDto>,
    val isActive: Boolean,
    val isGradable: Boolean,
)
