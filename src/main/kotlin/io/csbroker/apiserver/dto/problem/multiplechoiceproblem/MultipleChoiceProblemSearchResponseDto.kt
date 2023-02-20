package io.csbroker.apiserver.dto.problem.multiplechoiceproblem

data class MultipleChoiceProblemSearchResponseDto(
    val problems: List<MultipleChoiceProblemDataDto>,
    val totalPages: Int,
    val totalElements: Long,
) {
    data class MultipleChoiceProblemDataDto(
        val id: Long,
        val title: String,
        val creator: String,
        val answerRate: Double?,
        val userAnswerCnt: Int,
        val isActive: Boolean,
    )
}
