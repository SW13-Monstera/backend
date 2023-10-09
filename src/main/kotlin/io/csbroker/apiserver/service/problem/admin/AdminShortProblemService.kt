package io.csbroker.apiserver.service.problem.admin

import io.csbroker.apiserver.dto.problem.AdminProblemSearchDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemResponseDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemSearchResponseDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemUpsertRequestDto
import io.csbroker.apiserver.model.User

interface AdminShortProblemService {
    fun findProblems(problemSearchDto: AdminProblemSearchDto): ShortProblemSearchResponseDto
    fun findProblemById(id: Long): ShortProblemResponseDto
    fun createProblem(createRequestDto: ShortProblemUpsertRequestDto, user: User): Long
    fun updateProblem(id: Long, updateRequestDto: ShortProblemUpsertRequestDto): Long
}
