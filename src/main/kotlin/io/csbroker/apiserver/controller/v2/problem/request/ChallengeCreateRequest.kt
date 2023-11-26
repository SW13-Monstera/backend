package io.csbroker.apiserver.controller.v2.problem.request

import jakarta.validation.constraints.Size

data class ChallengeCreateRequest(
    @field:Size(min = 10, max = 300)
    val content: String,
)
