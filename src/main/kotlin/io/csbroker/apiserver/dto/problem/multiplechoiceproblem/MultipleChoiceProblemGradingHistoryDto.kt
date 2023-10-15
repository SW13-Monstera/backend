package io.csbroker.apiserver.dto.problem.multiplechoiceproblem

import io.csbroker.apiserver.dto.problem.ProblemCommonDetailResponse
import io.csbroker.apiserver.model.MultipleChoiceProblem

data class MultipleChoiceProblemGradingHistoryDto(
    val gradingHistoryId: Long,
    val problemId: Long,
    val title: String,
    val tags: List<String>,
    val description: String,
    val correctSubmission: Int,
    val correctUserCnt: Int,
    val totalSubmission: Int,
    val choices: List<ChoiceResponseDto>,
    val userAnswerIds: List<Long>,
    val isAnswer: Boolean,
    val score: Double,
) {

    companion object {
        fun createDto(
            gradingHistoryId: Long,
            problem: MultipleChoiceProblem,
            userAnswerIds: List<Long>,
            score: Double,
            isAnswer: Boolean,
        ): MultipleChoiceProblemGradingHistoryDto {
            val commonDetail = ProblemCommonDetailResponse.getCommonDetail(problem)

            val choices = problem.choicesList.map {
                ChoiceResponseDto(
                    it.id,
                    it.content,
                )
            }

            return MultipleChoiceProblemGradingHistoryDto(
                gradingHistoryId,
                problem.id,
                problem.title,
                commonDetail.tags,
                problem.description,
                commonDetail.correctSubmission,
                commonDetail.correctUserCnt,
                commonDetail.totalSubmission,
                choices,
                userAnswerIds,
                isAnswer,
                score,
            )
        }
    }
}
