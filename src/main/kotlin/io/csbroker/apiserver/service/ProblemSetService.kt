package io.csbroker.apiserver.service

import io.csbroker.apiserver.dto.problem.problemset.ProblemSetUpsertRequestDto
import io.csbroker.apiserver.model.ProblemSet

interface ProblemSetService {
    fun findAll(): List<ProblemSet>
    fun findById(id: Long): ProblemSet
    fun createProblemSet(problemSetUpsertRequestDto: ProblemSetUpsertRequestDto): Long
    fun updateProblemSet(id: Long, problemSetUpsertRequestDto: ProblemSetUpsertRequestDto): Long
}
