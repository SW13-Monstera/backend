package io.csbroker.apiserver.repository.problem

import io.csbroker.apiserver.dto.problem.ProblemResponseDto
import io.csbroker.apiserver.dto.problem.ProblemSearchDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ProblemRepositoryCustom {
    fun findProblemsByQuery(problemSearchDto: ProblemSearchDto, pageable: Pageable): Page<ProblemResponseDto>
}
