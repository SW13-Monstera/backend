package com.csbroker.apiserver.service

import com.csbroker.apiserver.dto.problem.ProblemPageResponseDto
import com.csbroker.apiserver.dto.problem.ProblemSearchDto
import com.csbroker.apiserver.dto.problem.longproblem.LongProblemDetailResponseDto
import com.csbroker.apiserver.dto.problem.longproblem.LongProblemGradingHistoryDto
import com.csbroker.apiserver.dto.problem.longproblem.LongProblemResponseDto
import com.csbroker.apiserver.dto.problem.longproblem.LongProblemSearchResponseDto
import com.csbroker.apiserver.dto.problem.longproblem.LongProblemUpsertRequestDto
import com.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemDetailResponseDto
import com.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemGradingHistoryDto
import com.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemResponseDto
import com.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemSearchResponseDto
import com.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemUpsertRequestDto
import com.csbroker.apiserver.dto.problem.shortproblem.ShortProblemDetailResponseDto
import com.csbroker.apiserver.dto.problem.shortproblem.ShortProblemGradingHistoryDto
import com.csbroker.apiserver.dto.problem.shortproblem.ShortProblemResponseDto
import com.csbroker.apiserver.dto.problem.shortproblem.ShortProblemSearchResponseDto
import com.csbroker.apiserver.dto.problem.shortproblem.ShortProblemUpsertRequestDto
import org.springframework.data.domain.Pageable

interface ProblemService {
    fun findProblems(problemSearchDto: ProblemSearchDto, pageable: Pageable): ProblemPageResponseDto
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

    fun findLongProblemDetailById(id: Long): LongProblemDetailResponseDto
    fun findShortProblemDetailById(id: Long): ShortProblemDetailResponseDto
    fun findMultipleChoiceProblemDetailById(id: Long): MultipleChoiceProblemDetailResponseDto
    fun findLongProblemById(id: Long): LongProblemResponseDto
    fun findShortProblemById(id: Long): ShortProblemResponseDto
    fun findMultipleProblemById(id: Long): MultipleChoiceProblemResponseDto
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

    fun gradingLongProblem(
        email: String,
        problemId: Long,
        answer: String
    ): LongProblemGradingHistoryDto

    fun gradingShortProblem(
        email: String,
        problemId: Long,
        answer: String
    ): ShortProblemGradingHistoryDto

    fun gradingMultipleChoiceProblem(
        email: String,
        problemId: Long,
        answerIds: List<Long>
    ): MultipleChoiceProblemGradingHistoryDto
}
