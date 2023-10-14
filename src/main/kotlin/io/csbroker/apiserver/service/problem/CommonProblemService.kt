package io.csbroker.apiserver.service.problem

import io.csbroker.apiserver.dto.problem.ProblemPageResponseDto
import io.csbroker.apiserver.dto.problem.ProblemSearchDto
import io.csbroker.apiserver.dto.problem.ProblemsResponseDto
import io.csbroker.apiserver.dto.problem.challenge.CreateChallengeDto
import io.csbroker.apiserver.dto.problem.grade.AssessmentRequestDto

interface CommonProblemService {
    fun findProblems(problemSearchDto: ProblemSearchDto): ProblemPageResponseDto
    fun findRandomProblems(size: Int): ProblemsResponseDto
    fun removeProblemById(id: Long)
    fun removeProblemsById(ids: List<Long>)
    fun gradingAssessment(email: String, gradingHistoryId: Long, assessmentRequestDto: AssessmentRequestDto): Long
    fun createChallenge(createChallengeDto: CreateChallengeDto)
    fun likeProblem(email: String, problemId: Long)
    fun bookmarkProblem(email: String, problemId: Long)
}
