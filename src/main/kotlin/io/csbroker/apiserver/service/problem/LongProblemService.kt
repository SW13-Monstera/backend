package io.csbroker.apiserver.service.problem

import io.csbroker.apiserver.controller.v2.problem.request.SubmitLongProblemDto
import io.csbroker.apiserver.controller.v2.problem.response.SubmitLongProblemResponseDto
import io.csbroker.apiserver.dto.problem.grade.LongProblemGradingRequestDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemDetailResponseDto

interface LongProblemService {
    fun findProblemById(id: Long, email: String?): LongProblemDetailResponseDto
    fun submitProblem(submitRequest: SubmitLongProblemDto): SubmitLongProblemResponseDto
}
