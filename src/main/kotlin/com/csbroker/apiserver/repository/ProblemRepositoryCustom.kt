package com.csbroker.apiserver.repository

import com.csbroker.apiserver.dto.problem.ProblemSearchDto
import com.csbroker.apiserver.model.Problem
import org.springframework.data.domain.Pageable

interface ProblemRepositoryCustom {
    fun findProblemsByQuery(problemSearchDto: ProblemSearchDto, pageable: Pageable): List<Problem>
}
