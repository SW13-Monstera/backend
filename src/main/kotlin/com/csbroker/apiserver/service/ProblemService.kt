package com.csbroker.apiserver.service

import com.csbroker.apiserver.dto.problem.LongProblemResponseDto
import com.csbroker.apiserver.dto.problem.LongProblemSearchResponseDto
import com.csbroker.apiserver.dto.problem.LongProblemUpsertRequestDto
import com.csbroker.apiserver.dto.problem.MultipleChoiceProblemSearchResponseDto
import com.csbroker.apiserver.dto.problem.MultipleChoiceProblemUpsertRequestDto
import com.csbroker.apiserver.dto.problem.MultipleProblemResponseDto
import com.csbroker.apiserver.dto.problem.ProblemDetailResponseDto
import com.csbroker.apiserver.dto.problem.ProblemResponseDto
import com.csbroker.apiserver.dto.problem.ProblemSearchDto
import com.csbroker.apiserver.dto.problem.ShortProblemResponseDto
import com.csbroker.apiserver.dto.problem.ShortProblemSearchResponseDto
import com.csbroker.apiserver.dto.problem.ShortProblemUpsertRequestDto
import org.springframework.data.domain.Pageable

interface ProblemService {
    fun findProblems(problemSearchDto: ProblemSearchDto, pageable: Pageable): List<ProblemResponseDto>
    fun findLongProblems(
        id: Long?,
        title: String?,
        description: String?,
        pageable: Pageable
    ): LongProblemSearchResponseDto

    fun findShortProblems(
        id: Long?,
        title: String?,
        description: String?,
        pageable: Pageable
    ): ShortProblemSearchResponseDto

    fun findMultipleProblems(
        id: Long?,
        title: String?,
        description: String?,
        pageable: Pageable
    ): MultipleChoiceProblemSearchResponseDto

    fun findProblemById(id: Long): ProblemDetailResponseDto?
    fun findLongProblemById(id: Long): LongProblemResponseDto
    fun findShortProblemById(id: Long): ShortProblemResponseDto
    fun findMultipleProblemById(id: Long): MultipleProblemResponseDto
    fun removeProblemById(id: Long)
    fun removeProblemsById(ids: List<Long>)
    fun createLongProblem(createRequestDto: LongProblemUpsertRequestDto, email: String): Long
    fun createShortProblem(createRequestDto: ShortProblemUpsertRequestDto, email: String): Long
    fun createMultipleChoiceProblem(createRequestDto: MultipleChoiceProblemUpsertRequestDto, email: String): Long

    fun updateLongProblem(id: Long, updateRequestDto: LongProblemUpsertRequestDto, email: String): Long
    fun updateShortProblem(id: Long, updateRequestDto: ShortProblemUpsertRequestDto, email: String): Long
    fun updateMultipleChoiceProblem(
        id: Long,
        updateRequestDto: MultipleChoiceProblemUpsertRequestDto,
        email: String
    ): Long
}
