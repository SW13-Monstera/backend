package io.csbroker.apiserver.service.problem.admin

import io.csbroker.apiserver.dto.problem.AdminProblemSearchDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemResponseDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemSearchResponseDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemUpsertRequestDto
import io.csbroker.apiserver.model.User

interface AdminMultipleProblemService {
    fun findProblems(problemSearchDto: AdminProblemSearchDto): MultipleChoiceProblemSearchResponseDto
    fun findProblemById(id: Long): MultipleChoiceProblemResponseDto
    fun createProblem(createRequestDto: MultipleChoiceProblemUpsertRequestDto, user: User): Long
    fun updateProblem(id: Long, updateRequestDto: MultipleChoiceProblemUpsertRequestDto): Long
}
