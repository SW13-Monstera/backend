package io.csbroker.apiserver.service.Problem

import io.csbroker.apiserver.dto.problem.ProblemDetailResponseDto
import io.csbroker.apiserver.dto.problem.ProblemGradingHistoryDto
import io.csbroker.apiserver.dto.problem.grade.GradingRequestDto

interface ProblemService2 {

    fun findProblemById(id: Long, email: String) : ProblemDetailResponseDto
    fun gradingProblem(gradingRequest: GradingRequestDto): ProblemGradingHistoryDto


}
