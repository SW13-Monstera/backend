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
            }.sorted()

            val correctUserCnt = problem.gradingHistory.groupBy {
                it.user.id
            }.count {
                it.value.any { gh -> gh.score == gh.problem.score }
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
