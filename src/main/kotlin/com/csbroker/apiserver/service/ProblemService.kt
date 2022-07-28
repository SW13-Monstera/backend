package com.csbroker.apiserver.service

import com.csbroker.apiserver.dto.problem.LongProblemCreateRequestDto
import com.csbroker.apiserver.dto.problem.MultipleChoiceProblemCreateRequestDto
import com.csbroker.apiserver.dto.problem.ProblemDetailResponseDto
import com.csbroker.apiserver.dto.problem.ProblemResponseDto
import com.csbroker.apiserver.dto.problem.ProblemSearchDto
import com.csbroker.apiserver.dto.problem.ShortProblemCreateRequestDto
import org.springframework.data.domain.Pageable

interface ProblemService {
    fun findProblems(problemSearchDto: ProblemSearchDto, pageable: Pageable): List<ProblemResponseDto>
    fun findProblemById(id: Long): ProblemDetailResponseDto?
    fun createLongProblem(createRequestDto: LongProblemCreateRequestDto, email: String): Long
    fun createShortProblem(createRequestDto: ShortProblemCreateRequestDto, email: String): Long
    fun createMultipleChoiceProblem(createRequestDto: MultipleChoiceProblemCreateRequestDto, email: String): Long
}
