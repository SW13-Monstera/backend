package io.csbroker.apiserver.controller.v2.problem.request


data class SubmitLongProblemDto(
    val email: String,
    val problemId: Long,
    val answer: String,
)
