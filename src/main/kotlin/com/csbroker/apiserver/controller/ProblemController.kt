package com.csbroker.apiserver.controller

import com.csbroker.apiserver.auth.LoginUser
import com.csbroker.apiserver.common.enums.ErrorCode
import com.csbroker.apiserver.common.exception.UnAuthorizedException
import com.csbroker.apiserver.dto.common.ApiResponse
import com.csbroker.apiserver.dto.common.UpsertSuccessResponseDto
import com.csbroker.apiserver.dto.problem.ProblemPageResponseDto
import com.csbroker.apiserver.dto.problem.ProblemSearchDto
import com.csbroker.apiserver.dto.problem.grade.AssessmentRequestDto
import com.csbroker.apiserver.dto.problem.longproblem.LongProblemAnswerDto
import com.csbroker.apiserver.dto.problem.longproblem.LongProblemDetailResponseDto
import com.csbroker.apiserver.dto.problem.longproblem.LongProblemGradingHistoryDto
import com.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemAnswerDto
import com.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemDetailResponseDto
import com.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemGradingHistoryDto
import com.csbroker.apiserver.dto.problem.shortproblem.ShortProblemAnswerDto
import com.csbroker.apiserver.dto.problem.shortproblem.ShortProblemDetailResponseDto
import com.csbroker.apiserver.dto.problem.shortproblem.ShortProblemGradingHistoryDto
import com.csbroker.apiserver.service.ProblemService
import org.springframework.data.domain.Pageable
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/problems")
class ProblemController(
    private val problemService: ProblemService
) {
    @GetMapping
    fun getAllProblemsByQuery(
        @RequestParam("query", required = false) query: String?,
        @RequestParam("isSolved", required = false) isSolved: Boolean?,
        @RequestParam("tags", required = false) tags: List<String>?,
        @RequestParam("type", required = false) type: List<String>?,
        @RequestParam("isGradable", required = false) isGradable: Boolean?,
        pageable: Pageable
    ): ApiResponse<ProblemPageResponseDto> {
        var solvedBy: String? = null

        if (isSolved != null) {
            solvedBy = this.getEmail() ?: throw UnAuthorizedException(ErrorCode.UNAUTHORIZED, "사용자 권한이 없습니다.")
        }

        val searchDto = ProblemSearchDto(tags, solvedBy, isSolved, query, type, isGradable)
        val foundProblems = this.problemService.findProblems(searchDto, pageable)

        return ApiResponse.success(foundProblems)
    }

    @GetMapping("/long/{id}")
    fun getLongProblemById(@PathVariable("id") id: Long): ApiResponse<LongProblemDetailResponseDto> {
        val findProblemDetail = this.problemService.findLongProblemDetailById(id, this.getEmail())

        return ApiResponse.success(findProblemDetail)
    }

    @GetMapping("/multiple/{id}")
    fun getMultipleProblemById(@PathVariable("id") id: Long): ApiResponse<MultipleChoiceProblemDetailResponseDto> {
        val findProblemDetail = this.problemService.findMultipleChoiceProblemDetailById(id, this.getEmail())

        return ApiResponse.success(findProblemDetail)
    }

    @GetMapping("/short/{id}")
    fun getShortProblemById(@PathVariable("id") id: Long): ApiResponse<ShortProblemDetailResponseDto> {
        val findProblemDetail = this.problemService.findShortProblemDetailById(id, this.getEmail())

        return ApiResponse.success(findProblemDetail)
    }

    @PostMapping("/long/{id}/grade")
    fun gradeLongProblem(
        @LoginUser loginUser: User,
        @PathVariable("id") id: Long,
        @RequestBody answerDto: LongProblemAnswerDto
    ): ApiResponse<LongProblemGradingHistoryDto> {
        val gradeHistory = this.problemService.gradingLongProblem(loginUser.username, id, answerDto.answer)
        return ApiResponse.success(gradeHistory)
    }

    @PostMapping("/short/{id}/grade")
    fun gradeShortProblem(
        @LoginUser loginUser: User,
        @PathVariable("id") id: Long,
        @RequestBody answerDto: ShortProblemAnswerDto
    ): ApiResponse<ShortProblemGradingHistoryDto> {
        val gradeHistory = this.problemService.gradingShortProblem(loginUser.username, id, answerDto.answer)
        return ApiResponse.success(gradeHistory)
    }

    @PostMapping("/multiple/{id}/grade")
    fun gradeMultipleChoiceProblem(
        @LoginUser loginUser: User,
        @PathVariable("id") id: Long,
        @RequestBody answerDto: MultipleChoiceProblemAnswerDto
    ): ApiResponse<MultipleChoiceProblemGradingHistoryDto> {
        val gradeHistory = this.problemService.gradingMultipleChoiceProblem(loginUser.username, id, answerDto.answerIds)
        return ApiResponse.success(gradeHistory)
    }

    @PostMapping("/grade/{id}/assessment")
    fun gradingAssessment(
        @LoginUser loginUser: User,
        @PathVariable("id") id: Long,
        @RequestBody assessmentRequestDto: AssessmentRequestDto
    ): ApiResponse<UpsertSuccessResponseDto> {
        return ApiResponse.success(
            UpsertSuccessResponseDto(
                this.problemService.gradingAssessment(
                    loginUser.username,
                    id,
                    assessmentRequestDto
                )
            )
        )
    }

    private fun getEmail(): String? {
        return try {
            val principal = SecurityContextHolder.getContext().authentication.principal
                as User
            principal.username
        } catch (e: Exception) {
            null
        }
    }
}
