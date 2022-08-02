package com.csbroker.apiserver.dto.problem

import com.csbroker.apiserver.dto.user.GradingStandardResponseDto

data class LongProblemResponseDto(
    val id: Long,
    val title: String,
    val description: String,
    val standardAnswer: String,
    val tags: List<String>,
    val gradingStandards: List<GradingStandardResponseDto>
)
