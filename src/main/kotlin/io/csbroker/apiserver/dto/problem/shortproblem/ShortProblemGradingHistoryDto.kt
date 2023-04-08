package io.csbroker.apiserver.dto.problem.shortproblem

import io.csbroker.apiserver.dto.problem.ProblemCommonDetailResponse
import io.csbroker.apiserver.dto.problem.ProblemGradingHistoryDto
import io.csbroker.apiserver.model.ShortProblem

data class ShortProblemGradingHistoryDto(
    val gradingHistoryId: Long,
    val problemId: Long,
    val title: String,
    val tags: List<String>,
    val description: String,
    val correctSubmission: Int,
    val correctUserCnt: Int,
    val totalSubmission: Int,
    val userAnswer: String,
    val answerLength: Int,
    val isAnswer: Boolean,
    val score: Double,
    val correctAnswer: String,
) : ProblemGradingHistoryDto {

    companion object {
        fun createDto(
            gradingHistoryId: Long,
            problem: ShortProblem,
            userAnswer: String,
            score: Double,
            isAnswer: Boolean,
        ): ShortProblemGradingHistoryDto {
            val commonDetail = ProblemCommonDetailResponse.getCommonDetail(problem)

            return ShortProblemGradingHistoryDto(
                gradingHistoryId,
                problem.id!!,
                problem.title,
                commonDetail.tags,
                problem.description,
                commonDetail.correctSubmission,
                commonDetail.correctUserCnt,
                commonDetail.totalSubmission,
                userAnswer,
                problem.answer.length,
                isAnswer,
                score,
                problem.answer,
            )
        }
    }
}
