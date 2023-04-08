package io.csbroker.apiserver.service.problem

import io.csbroker.apiserver.dto.problem.grade.MultipleProblemGradingRequestDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemDetailResponseDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemGradingHistoryDto

interface MultipleProblemService {
    fun findProblemById(id: Long, email: String?): MultipleChoiceProblemDetailResponseDto
    fun gradingProblem(gradingRequest: MultipleProblemGradingRequestDto): MultipleChoiceProblemGradingHistoryDto
}
