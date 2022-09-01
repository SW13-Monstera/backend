package com.csbroker.apiserver.dto.problem.longproblem

import com.csbroker.apiserver.model.LongProblem

data class LongProblemGradingHistoryDto(
    val gradingHistoryId: Long,
    val problemId: Long,
    val title: String,
    val tags: List<String>,
    val description: String,
    val avgScore: Double?,
    val topScore: Double?,
    val bottomScore: Double?,
    val totalSolved: Int,
    val score: Double,
    val keywords: List<KeywordDto>,
    val userAnswer: String,
    val standardAnswer: String
) {

    companion object {
        fun createDto(
            gradingHistoryId: Long,
            problem: LongProblem,
            userAnswer: String,
            score: Double,
            keywords: List<KeywordDto>
        ): LongProblemGradingHistoryDto {
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

            return LongProblemGradingHistoryDto(
                gradingHistoryId,
                problem.id!!,
                problem.title,
                tags,
                problem.description,
                if (scoreList.isEmpty()) null else scoreList.average(),
                if (scoreList.isEmpty()) null else scoreList.last(),
                if (scoreList.isEmpty()) null else scoreList.first(),
                totalSolved,
                score,
                keywords,
                userAnswer,
                problem.standardAnswer
            )
        }
    }
}