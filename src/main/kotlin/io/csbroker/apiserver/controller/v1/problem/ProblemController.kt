package io.csbroker.apiserver.controller.v1.problem

import io.csbroker.apiserver.auth.LoginUser
import io.csbroker.apiserver.common.enums.ErrorCode
import io.csbroker.apiserver.common.exception.UnAuthorizedException
import io.csbroker.apiserver.common.util.getEmailFromSecurityContextHolder
import io.csbroker.apiserver.dto.common.ApiResponse
import io.csbroker.apiserver.dto.common.UpsertSuccessResponseDto
import io.csbroker.apiserver.dto.problem.ProblemPageResponseDto
import io.csbroker.apiserver.dto.problem.ProblemSearchDto
import io.csbroker.apiserver.dto.problem.ProblemsResponseDto
import io.csbroker.apiserver.dto.problem.grade.AssessmentRequestDto
import io.csbroker.apiserver.model.User
import io.csbroker.apiserver.service.problem.CommonProblemService
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
    private val commonProblemService: CommonProblemService,
) {
    @GetMapping
    fun getAllProblemsByQuery(
        @RequestParam("query", required = false) query: String?,
        @RequestParam("isSolved", required = false) isSolved: Boolean?,
        @RequestParam("tags", required = false) tags: List<String>?,
        @RequestParam("type", required = false) type: List<String>?,
        @RequestParam("isGradable", required = false) isGradable: Boolean?,
        @RequestParam("page") page: Int,
        @RequestParam("size") size: Int,
    ): ApiResponse<ProblemPageResponseDto> {
        val solvedBy = isSolved?.let {
            getEmailFromSecurityContextHolder()
                ?: throw UnAuthorizedException(ErrorCode.FORBIDDEN, "사용자 권한이 없습니다.")
        }
        val searchDto = ProblemSearchDto(tags, solvedBy, isSolved, query, type, isGradable, page, size)
        val foundProblems = commonProblemService.findProblems(searchDto)

        return ApiResponse.success(foundProblems)
    }

    @GetMapping("/shuffle")
    fun getRandomProblems(@RequestParam("size") size: Int): ApiResponse<ProblemsResponseDto> {
        val problemsResponseDto = commonProblemService.findRandomProblems(size)
        return ApiResponse.success(problemsResponseDto)
    }

    @PostMapping("/grade/{id}/assessment")
    fun gradingAssessment(
        @LoginUser loginUser: User,
        @PathVariable("id") id: Long,
        @RequestBody assessmentRequestDto: AssessmentRequestDto,
    ): ApiResponse<UpsertSuccessResponseDto> {
        return ApiResponse.success(
            UpsertSuccessResponseDto(
                commonProblemService.gradingAssessment(
                    loginUser.email,
                    id,
                    assessmentRequestDto,
                ),
            ),
        )
    }
}
