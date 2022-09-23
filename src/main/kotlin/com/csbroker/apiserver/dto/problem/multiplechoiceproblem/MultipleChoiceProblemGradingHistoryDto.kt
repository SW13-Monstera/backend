package com.csbroker.apiserver.dto.problem.multiplechoiceproblem

import com.csbroker.apiserver.model.MultipleChoiceProblem

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

            var correctUserCnt = 0

            problem.gradingHistory.groupBy {
                it.user.id
            }.forEach {
                if (it.value.any { gh -> gh.score == gh.problem.score }) {
                    correctUserCnt++
                }
            }

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
                scoreList.count { it == problem.score },
                correctUserCnt,
                scoreList.size,
                choices,
                userAnswerIds,
                isAnswer,
                score
            )
        }
    }
}
