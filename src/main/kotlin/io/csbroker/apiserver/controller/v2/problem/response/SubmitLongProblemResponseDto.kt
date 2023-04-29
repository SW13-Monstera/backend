package io.csbroker.apiserver.controller.v2.problem.response

data class SubmitLongProblemResponseDto(
    val title: String,
    val tags: List<String>,
    val description: String,
    val totalSubmissionCount: Int,
    val userSubmissionCount: Int,
    val userAnswer: String,
    val standardAnswer: String,
)
