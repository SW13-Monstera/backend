package io.csbroker.apiserver.service.problem

import io.csbroker.apiserver.dto.problem.grade.GradingRequestDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemDetailResponseDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemGradingHistoryDto

interface LongProblemService {
    fun findProblemById(id: Long, email: String?): LongProblemDetailResponseDto
    fun gradingProblem(gradingRequest: GradingRequestDto): LongProblemGradingHistoryDto
}
