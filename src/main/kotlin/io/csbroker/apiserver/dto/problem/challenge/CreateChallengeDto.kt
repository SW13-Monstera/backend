package io.csbroker.apiserver.dto.problem.challenge

data class CreateChallengeDto(
    val email: String,
    val problemId: Long,
    val content: String,
)
