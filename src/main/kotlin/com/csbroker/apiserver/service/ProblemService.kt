package com.csbroker.apiserver.service

import com.csbroker.apiserver.dto.LongProblemCreateRequestDto
import com.csbroker.apiserver.dto.MultipleChoiceProblemCreateRequestDto
import com.csbroker.apiserver.dto.ProblemDetailResponseDto
import com.csbroker.apiserver.dto.ProblemResponseDto
import com.csbroker.apiserver.dto.ProblemSearchDto
import com.csbroker.apiserver.dto.ShortProblemCreateRequestDto
import org.springframework.data.domain.Pageable

interface ProblemService {
    fun findProblems(problemSearchDto: ProblemSearchDto, pageable: Pageable): List<ProblemResponseDto>
    fun findProblemById(id: Long): ProblemDetailResponseDto?
    fun createLongProblem(createRequestDto: LongProblemCreateRequestDto, email: String): Long
    fun createShortProblem(createRequestDto: ShortProblemCreateRequestDto, email: String): Long
    fun createMultipleChoiceProblem(createRequestDto: MultipleChoiceProblemCreateRequestDto, email: String): Long
}
