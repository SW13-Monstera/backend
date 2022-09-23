package com.csbroker.apiserver.dto.problem.shortproblem

import com.csbroker.apiserver.model.ShortProblem

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
    val score: Double
) {

    companion object {
        fun createDto(
            gradingHistoryId: Long,
            problem: ShortProblem,
            userAnswer: String,
            score: Double,
            isAnswer: Boolean
        ): ShortProblemGradingHistoryDto {
            val tags = problem.problemTags.map {
                it.tag
            }.map {
                it.name
            }

            val scoreList = problem.gradingHistory.map {
                it.score
            }.toList().sorted()

            var correctUserCnt = 0

            problem.gradingHistory.groupBy {
                it.user.id
            }.forEach {
                if (it.value.any { gh -> gh.score == gh.problem.score }) {
                    correctUserCnt++
                }
            }

            return ShortProblemGradingHistoryDto(
                gradingHistoryId,
                problem.id!!,
                problem.title,
                tags,
                problem.description,
                scoreList.count { it == problem.score },
                correctUserCnt,
                scoreList.size,
                userAnswer,
                problem.answer.length,
                isAnswer,
                score
            )
        }
    }
}
