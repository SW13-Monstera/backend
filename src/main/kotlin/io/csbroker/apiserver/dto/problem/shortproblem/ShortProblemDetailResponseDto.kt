package io.csbroker.apiserver.dto.problem.shortproblem

import io.csbroker.apiserver.dto.problem.ProblemDetailResponseDto

data class ShortProblemDetailResponseDto(
    val id: Long,
    val title: String,
    val tags: List<String>,
    val description: String,
    val correctSubmission: Int,
    val correctUserCnt: Int,
    val totalSubmission: Int,
    val answerLength: Int,
    val isEnglish: Boolean,
    val isSolved: Boolean,
    val score: Double,
) : ProblemDetailResponseDto
