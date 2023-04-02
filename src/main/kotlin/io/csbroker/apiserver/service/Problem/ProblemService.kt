package io.csbroker.apiserver.service.Problem

import io.csbroker.apiserver.controller.v2.response.ShortProblemDetailResponseV2Dto
import io.csbroker.apiserver.dto.problem.ProblemPageResponseDto
import io.csbroker.apiserver.dto.problem.ProblemSearchDto
import io.csbroker.apiserver.dto.problem.challenge.CreateChallengeDto
import io.csbroker.apiserver.dto.problem.grade.AssessmentRequestDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemDetailResponseDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemGradingHistoryDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemResponseDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemSearchResponseDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemUpsertRequestDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemDetailResponseDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemGradingHistoryDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemResponseDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemSearchResponseDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemUpsertRequestDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemDetailResponseDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemGradingHistoryDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemResponseDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemSearchResponseDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemUpsertRequestDto
import org.springframework.data.domain.Pageable

interface ProblemService {
    fun findProblems(problemSearchDto: ProblemSearchDto, pageable: Pageable): ProblemPageResponseDto
    fun findLongProblems(
        id: Long?,
        title: String?,
        description: String?,
        pageable: Pageable,
    ): LongProblemSearchResponseDto

    fun findShortProblems(
        id: Long?,
        title: String?,
        description: String?,
        pageable: Pageable,
    ): ShortProblemSearchResponseDto

    fun findMultipleProblems(
        id: Long?,
        title: String?,
        description: String?,
        pageable: Pageable,
    ): MultipleChoiceProblemSearchResponseDto

    fun findLongProblemDetailById(id: Long, email: String?): LongProblemDetailResponseDto
    fun findShortProblemDetailById(id: Long, email: String?): ShortProblemDetailResponseDto
    fun findShortProblemDetailByIdV2(id: Long, email: String?): ShortProblemDetailResponseV2Dto
    fun findMultipleChoiceProblemDetailById(id: Long, email: String?): MultipleChoiceProblemDetailResponseDto
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
        email: String,
    ): Long

    fun gradingLongProblem(
        email: String,
        problemId: Long,
        answer: String,
        isGrading: Boolean,
    ): LongProblemGradingHistoryDto

    fun gradingShortProblem(
        email: String,
        problemId: Long,
        answer: String,
    ): ShortProblemGradingHistoryDto

    fun gradingMultipleChoiceProblem(
        email: String,
        problemId: Long,
        answerIds: List<Long>,
    ): MultipleChoiceProblemGradingHistoryDto

    fun gradingAssessment(
        email: String,
        gradingHistoryId: Long,
        assessmentRequestDto: AssessmentRequestDto,
    ): Long

    fun createChallenge(createChallengeDto: CreateChallengeDto)
}
