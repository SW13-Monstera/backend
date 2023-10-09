package io.csbroker.apiserver.dto.problem.challenge

import io.csbroker.apiserver.model.User

data class CreateChallengeDto(
    val user: User,
    val problemId: Long,
    val content: String,
)
