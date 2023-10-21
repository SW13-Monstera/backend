package io.csbroker.apiserver.service.problem.admin

import io.csbroker.apiserver.dto.problem.AdminProblemSearchDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemResponseDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemSearchResponseDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemUpsertRequestDto
import io.csbroker.apiserver.model.User

interface AdminLongProblemService {
    fun findProblems(problemSearchDto: AdminProblemSearchDto): LongProblemSearchResponseDto
    fun findProblemById(id: Long): LongProblemResponseDto
    fun createProblem(createRequestDto: LongProblemUpsertRequestDto, user: User): Long
    fun updateProblem(id: Long, updateRequestDto: LongProblemUpsertRequestDto): Long
}
