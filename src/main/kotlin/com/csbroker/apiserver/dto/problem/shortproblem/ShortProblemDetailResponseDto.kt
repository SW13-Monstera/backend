package com.csbroker.apiserver.dto.problem.shortproblem

data class ShortProblemDetailResponseDto(
    val id: Long,
    val title: String,
    val tags: List<String>,
    val description: String,
    val correctCnt: Int,
    val wrongCnt: Int,
    val totalSolved: Int,
    val answerLength: Int,
    val isEnglish: Boolean
)
