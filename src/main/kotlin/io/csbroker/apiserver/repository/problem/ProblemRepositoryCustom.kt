package io.csbroker.apiserver.repository.problem

import io.csbroker.apiserver.dto.problem.ProblemResponseDto
import io.csbroker.apiserver.dto.problem.ProblemSearchDto
import org.springframework.data.domain.Page

interface ProblemRepositoryCustom {
    fun findProblemsByQuery(problemSearchDto: ProblemSearchDto): Page<ProblemResponseDto>
}
