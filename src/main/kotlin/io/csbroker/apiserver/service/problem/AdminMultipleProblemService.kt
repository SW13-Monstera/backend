package io.csbroker.apiserver.service.problem

import io.csbroker.apiserver.dto.problem.AdminProblemSearchDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemResponseDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemSearchResponseDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemUpsertRequestDto

interface AdminMultipleProblemService {
    fun findProblems(problemSearchDto: AdminProblemSearchDto): MultipleChoiceProblemSearchResponseDto
    fun findProblemById(id: Long): MultipleChoiceProblemResponseDto
    fun createProblem(createRequestDto: MultipleChoiceProblemUpsertRequestDto, email: String): Long
    fun updateProblem(id: Long, updateRequestDto: MultipleChoiceProblemUpsertRequestDto, email: String): Long
}
