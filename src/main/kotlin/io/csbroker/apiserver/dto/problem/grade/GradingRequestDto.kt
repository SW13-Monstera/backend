package io.csbroker.apiserver.dto.problem.grade

import io.csbroker.apiserver.model.User

interface GradingRequestDto

data class MultipleProblemGradingRequestDto(
    val user: User,
    val problemId: Long,
    val answerIds: List<Long>,
) : GradingRequestDto

data class ShortProblemGradingRequestDto(
    val user: User,
    val problemId: Long,
    val answer: String,
) : GradingRequestDto

