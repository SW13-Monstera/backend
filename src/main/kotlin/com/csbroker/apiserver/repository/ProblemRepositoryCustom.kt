package com.csbroker.apiserver.repository

import com.csbroker.apiserver.dto.problem.ProblemResponseDto
import com.csbroker.apiserver.dto.problem.ProblemSearchDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ProblemRepositoryCustom {
    fun findProblemsByQuery(problemSearchDto: ProblemSearchDto, pageable: Pageable): Page<ProblemResponseDto>
}
