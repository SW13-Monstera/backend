package io.csbroker.apiserver.service

import io.csbroker.apiserver.dto.problem.ProblemPageResponseDto
import io.csbroker.apiserver.dto.problem.ProblemSearchDto
import io.csbroker.apiserver.dto.problem.challenge.CreateChallengeDto
import io.csbroker.apiserver.dto.problem.grade.AssessmentRequestDto
import org.springframework.data.domain.Pageable

interface IntegrationProblemService {
    fun findProblems(problemSearchDto: ProblemSearchDto, pageable: Pageable): ProblemPageResponseDto

    fun removeProblemById(id: Long)
    fun removeProblemsById(ids: List<Long>)
    fun gradingAssessment(email: String, gradingHistoryId: Long, assessmentRequestDto: AssessmentRequestDto): Long
    fun createChallenge(createChallengeDto: CreateChallengeDto)

}
