package io.csbroker.apiserver.dto.problem

import io.csbroker.apiserver.model.Problem

data class ProblemCommonDetailResponse(
    val tags: List<String>,
    val correctSubmission: Int,
    val correctUserCnt: Int,
    val totalSubmission: Int
) {
    companion object {
        fun getCommonDetail(problem: Problem): ProblemCommonDetailResponse {
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

            return ProblemCommonDetailResponse(
                problem.problemTags.map {
                    it.tag
                }.map {
                    it.name
                },
                scoreList.count { it == problem.score },
                correctUserCnt,
                scoreList.size
            )
        }
    }
}
