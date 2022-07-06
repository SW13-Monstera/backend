package com.csbroker.apiserver.service

import com.csbroker.apiserver.dto.ProblemDetailResponseDto
import com.csbroker.apiserver.dto.ProblemResponseDto
import com.csbroker.apiserver.dto.ProblemSearchDto
import java.util.UUID

interface ProblemService {
    fun findProblems(problemSearchDto: ProblemSearchDto): List<ProblemResponseDto>
    fun findProblemById(id: UUID): ProblemDetailResponseDto?
}
