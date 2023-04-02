package io.csbroker.apiserver.controller.v1

import io.csbroker.apiserver.auth.LoginUser
import io.csbroker.apiserver.common.enums.ErrorCode
import io.csbroker.apiserver.common.exception.UnAuthorizedException
import io.csbroker.apiserver.common.util.getEmailFromSecurityContextHolder
import io.csbroker.apiserver.dto.common.ApiResponse
import io.csbroker.apiserver.dto.common.UpsertSuccessResponseDto
import io.csbroker.apiserver.dto.problem.ProblemPageResponseDto
import io.csbroker.apiserver.dto.problem.ProblemSearchDto
import io.csbroker.apiserver.dto.problem.grade.AssessmentRequestDto
import io.csbroker.apiserver.dto.problem.grade.LongProblemGradingRequestDto
import io.csbroker.apiserver.dto.problem.grade.MultipleProblemGradingRequestDto
import io.csbroker.apiserver.dto.problem.grade.ShortProblemGradingRequestDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemAnswerDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemDetailResponseDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemGradingHistoryDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemAnswerDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemDetailResponseDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemGradingHistoryDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemAnswerDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemDetailResponseDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemGradingHistoryDto
import io.csbroker.apiserver.service.Problem.CommonProblemService
import io.csbroker.apiserver.service.Problem.ProblemService2
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.Pageable
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
class ProblemController2(
    @Qualifier("longProblemService") private val longProblemService: ProblemService2,
    @Qualifier("shortProblemService") private val shortProblemService: ProblemService2,
    @Qualifier("multipleProblemService") private val multipleProblemService: ProblemService2,
    private val problemService: CommonProblemService,
) {
    @GetMapping
    fun getAllProblemsByQuery(
        @RequestParam("query", required = false) query: String?,
        @RequestParam("isSolved", required = false) isSolved: Boolean?,
        @RequestParam("tags", required = false) tags: List<String>?,
        @RequestParam("type", required = false) type: List<String>?,
        @RequestParam("isGradable", required = false) isGradable: Boolean?,
        pageable: Pageable,
    ): ApiResponse<ProblemPageResponseDto> {
        var solvedBy: String? = null

        if (isSolved != null) {
            solvedBy = getEmailFromSecurityContextHolder()
                ?: throw UnAuthorizedException(ErrorCode.FORBIDDEN, "사용자 권한이 없습니다.")
        }

        val searchDto = ProblemSearchDto(tags, solvedBy, isSolved, query, type, isGradable)
        val foundProblems = problemService.findProblems(searchDto, pageable)

        return ApiResponse.success(foundProblems)
    }

    @GetMapping("/long/{id}")
    fun getLongProblemById(@PathVariable("id") id: Long): ApiResponse<LongProblemDetailResponseDto> {

        val findProblemDetail = longProblemService.findProblemById(id, getEmailFromSecurityContextHolder())

        return ApiResponse.success(findProblemDetail as LongProblemDetailResponseDto)
    }

    @GetMapping("/multiple/{id}")
    fun getMultipleProblemById(@PathVariable("id") id: Long): ApiResponse<MultipleChoiceProblemDetailResponseDto> {
        val findProblemDetail =
            multipleProblemService.findProblemById(id, getEmailFromSecurityContextHolder())

        return ApiResponse.success(findProblemDetail as MultipleChoiceProblemDetailResponseDto)
    }

    @GetMapping("/short/{id}")
    fun getShortProblemById(@PathVariable("id") id: Long): ApiResponse<ShortProblemDetailResponseDto> {
        val findProblemDetail = shortProblemService.findProblemById(id, getEmailFromSecurityContextHolder())

        return ApiResponse.success(findProblemDetail as ShortProblemDetailResponseDto)
    }

    @PostMapping("/long/{id}/grade")
    fun gradeLongProblem(
        @LoginUser loginUser: User,
        @PathVariable("id") id: Long,
        @RequestBody answerDto: LongProblemAnswerDto,
        @RequestParam("isGrading", required = false) isGrading: Boolean?,
    ): ApiResponse<LongProblemGradingHistoryDto> {
        val gradingRequestDto = LongProblemGradingRequestDto(
            loginUser.username,
            id,
            answerDto.answer,
            isGrading ?: true
            )
        val gradeHistory = longProblemService.gradingProblem(gradingRequestDto)
        return ApiResponse.success(gradeHistory as LongProblemGradingHistoryDto)
    }

    @PostMapping("/short/{id}/grade")
    fun gradeShortProblem(
        @LoginUser loginUser: User,
        @PathVariable("id") id: Long,
        @RequestBody answerDto: ShortProblemAnswerDto,
    ): ApiResponse<ShortProblemGradingHistoryDto> {
        val gradingRequestDto = ShortProblemGradingRequestDto(loginUser.username, id, answerDto.answer)
        val gradeHistory = shortProblemService.gradingProblem(gradingRequestDto)
        return ApiResponse.success(gradeHistory as ShortProblemGradingHistoryDto)
    }

    @PostMapping("/multiple/{id}/grade")
    fun gradeMultipleChoiceProblem(
        @LoginUser loginUser: User,
        @PathVariable("id") id: Long,
        @RequestBody answerDto: MultipleChoiceProblemAnswerDto,
    ): ApiResponse<MultipleChoiceProblemGradingHistoryDto> {
        val gradingRequestDto = MultipleProblemGradingRequestDto(loginUser.username, id, answerDto.answerIds)
        val gradeHistory = multipleProblemService.gradingProblem(gradingRequestDto)
        return ApiResponse.success(gradeHistory as MultipleChoiceProblemGradingHistoryDto)
    }

    @PostMapping("/grade/{id}/assessment")
    fun gradingAssessment(
        @LoginUser loginUser: User,
        @PathVariable("id") id: Long,
        @RequestBody assessmentRequestDto: AssessmentRequestDto,
    ): ApiResponse<UpsertSuccessResponseDto> {
        return ApiResponse.success(
            UpsertSuccessResponseDto(
                problemService.gradingAssessment(
                    loginUser.username,
                    id,
                    assessmentRequestDto,
                ),
            ),
        )
    }
}
