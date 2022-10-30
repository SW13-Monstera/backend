package io.csbroker.apiserver.dto.problem.longproblem

import io.csbroker.apiserver.model.LongProblem

data class LongProblemGradingHistoryDto(
    val gradingHistoryId: Long,
    val problemId: Long,
    val title: String,
    val tags: List<String>,
    val description: String,
    val avgScore: Double?,
    val topScore: Double?,
    val bottomScore: Double?,
    val totalSubmission: Int,
    val score: Double,
    val keywords: List<KeywordDto>,
    val contents: List<ContentDto>,
    val userAnswer: String,
    val standardAnswer: String
) {

    companion object {
        fun createDto(
            gradingHistoryId: Long,
            problem: LongProblem,
            userAnswer: String,
            score: Double,
            keywords: List<KeywordDto>,
            contents: List<ContentDto>
        ): LongProblemGradingHistoryDto {
            val tags = problem.problemTags.map {
                it.tag
            }.map {
                it.name
            }

            val scoreList = problem.gradingHistory.map {
                it.score
            }.toList().sorted()

            return LongProblemGradingHistoryDto(
                gradingHistoryId,
                problem.id!!,
                problem.title,
                tags,
                problem.description,
                if (scoreList.isEmpty()) null else scoreList.average(),
                if (scoreList.isEmpty()) null else scoreList.last(),
                if (scoreList.isEmpty()) null else scoreList.first(),
                scoreList.size,
                score,
                keywords,
                contents,
                userAnswer,
                problem.standardAnswer
            )
        }
    }
}
