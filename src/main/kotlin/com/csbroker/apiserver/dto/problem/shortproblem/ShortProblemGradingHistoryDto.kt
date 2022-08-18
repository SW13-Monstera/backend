package com.csbroker.apiserver.dto.problem.shortproblem

import com.csbroker.apiserver.model.ShortProblem

data class ShortProblemGradingHistoryDto(
    val gradingHistoryId: Long,
    val problemId: Long,
    val title: String,
    val tags: List<String>,
    val description: String,
    val correctCnt: Int,
    val wrongCnt: Int,
    val totalSolved: Int,
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

            val totalSolved = problem.gradingHistory.map {
                it.user.username
            }.distinct().size

            return ShortProblemGradingHistoryDto(
                gradingHistoryId,
                problem.id!!,
                problem.title,
                tags,
                problem.description,
                scoreList.count { it == problem.score },
                scoreList.count { it != problem.score },
                totalSolved,
                userAnswer,
                problem.answer.length,
                isAnswer,
                score
            )
        }
    }
}
