package com.csbroker.apiserver.dto.problem

data class MultipleChoiceProblemDetailResponseDto(
    val id: Long,
    val title: String,
    val tags: List<String>,
    val description: String,
    val avgScore: Double?,
    val topScore: Double?,
    val bottomScore: Double?,
    val totalSolved: Int,
    val choices: List<ChoiceResponseDto>
)
