package io.csbroker.apiserver.service.problem

import io.csbroker.apiserver.controller.v2.problem.response.ShortProblemDetailResponseV2Dto
import io.csbroker.apiserver.dto.problem.grade.GradingRequestDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemDetailResponseDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemGradingHistoryDto

interface ShortProblemService {
    fun findProblemById(id: Long, email: String?): ShortProblemDetailResponseDto
    fun gradingProblem(gradingRequest: GradingRequestDto): ShortProblemGradingHistoryDto
    fun findShortProblemDetailByIdV2(id: Long, email: String?): ShortProblemDetailResponseV2Dto
}
