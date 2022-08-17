package com.csbroker.apiserver.dto.problem

import com.csbroker.apiserver.model.MultipleChoiceProblem

data class MultipleChoiceProblemGradingHistoryDto(
    val gradingHistoryId: Long,
    val problemId: Long,
    val title: String,
    val tags: List<String>,
    val description: String,
    val avgScore: Double?,
    val topScore: Double?,
    val bottomScore: Double?,
    val totalSolved: Int,
    val choices: List<ChoiceResponseDto>,
    val userAnswerIds: List<Long>,
    val isAnswer: Boolean,
    val score: Double
) {

    companion object {
        fun createDto(
            gradingHistoryId: Long,
            problem: MultipleChoiceProblem,
            userAnswerIds: List<Long>,
            score: Double,
            isAnswer: Boolean
        ): MultipleChoiceProblemGradingHistoryDto {
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

            val choices = problem.choicesList.map {
                ChoiceResponseDto(
                    it.id!!,
                    it.content
                )
            }

            return MultipleChoiceProblemGradingHistoryDto(
                gradingHistoryId,
                problem.id!!,
                problem.title,
                tags,
                problem.description,
                if (scoreList.isEmpty()) null else scoreList.average(),
                if (scoreList.isEmpty()) null else scoreList.first(),
                if (scoreList.isEmpty()) null else scoreList.last(),
                totalSolved,
                choices,
                userAnswerIds,
                isAnswer,
                score
            )
        }
    }
}
