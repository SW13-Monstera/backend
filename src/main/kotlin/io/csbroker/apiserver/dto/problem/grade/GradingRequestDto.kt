package io.csbroker.apiserver.dto.problem.grade

interface GradingRequestDto

data class MultipleProblemGradingRequestDto(
    val email: String,
    val problemId: Long,
    val answerIds: List<Long>
) : GradingRequestDto


data class ShortProblemGradingRequestDto(
    val email: String,
    val problemId: Long,
    val answer: String
) : GradingRequestDto


data class LongProblemGradingRequestDto(
    val email: String,
    val problemId: Long,
    val answer: String,
    val isGrading: Boolean,
) : GradingRequestDto

