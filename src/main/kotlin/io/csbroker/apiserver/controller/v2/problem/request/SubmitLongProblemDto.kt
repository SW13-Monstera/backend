package io.csbroker.apiserver.controller.v2.problem.request

import io.csbroker.apiserver.model.User

data class SubmitLongProblemDto(
    val user: User,
    val problemId: Long,
    val answer: String,
)
