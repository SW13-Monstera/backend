package com.csbroker.apiserver.service

import com.csbroker.apiserver.dto.ProblemDetailResponseDto
import com.csbroker.apiserver.dto.ProblemResponseDto
import com.csbroker.apiserver.dto.ProblemSearchDto
import org.springframework.data.domain.Pageable
import java.util.*

interface ProblemService {
    fun findProblems(problemSearchDto: ProblemSearchDto, pageable: Pageable): List<ProblemResponseDto>
    fun findProblemById(id: UUID): ProblemDetailResponseDto?
}
