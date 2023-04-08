package io.csbroker.apiserver.dto.problem.multiplechoiceproblem

import io.csbroker.apiserver.dto.problem.ProblemDetailResponseDto

data class MultipleChoiceProblemDetailResponseDto(
    val id: Long,
    val title: String,
    val tags: List<String>,
    val description: String,
    val correctSubmission: Int,
    val correctUserCnt: Int,
    val totalSubmission: Int,
    val choices: List<ChoiceResponseDto>,
    val isSolved: Boolean,
    val isMultipleAnswer: Boolean,
    val score: Double,
) : ProblemDetailResponseDto
