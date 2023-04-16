package io.csbroker.apiserver.repository.problem

import io.csbroker.apiserver.dto.problem.GradingHistoryStats
import io.csbroker.apiserver.dto.problem.ProblemResponseDto
import io.csbroker.apiserver.dto.problem.ProblemSearchDto
import io.csbroker.apiserver.model.Problem
import org.springframework.data.domain.Page

interface ProblemRepositoryCustom {
    fun findProblemsByQuery(problemSearchDto: ProblemSearchDto): Page<ProblemResponseDto>
    fun getProblemId2StatMap(problems: List<Problem>): Map<Long?, GradingHistoryStats>
}
