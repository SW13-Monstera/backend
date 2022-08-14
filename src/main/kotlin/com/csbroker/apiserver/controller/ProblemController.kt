package com.csbroker.apiserver.controller

import com.csbroker.apiserver.common.auth.LoginUser
import com.csbroker.apiserver.common.enums.ErrorCode
import com.csbroker.apiserver.common.exception.EntityNotFoundException
import com.csbroker.apiserver.common.exception.UnAuthorizedException
import com.csbroker.apiserver.dto.ApiResponse
import com.csbroker.apiserver.dto.problem.LongProblemAnswerDto
import com.csbroker.apiserver.dto.problem.LongProblemGradingHistoryDto
import com.csbroker.apiserver.dto.problem.ProblemDetailResponseDto
import com.csbroker.apiserver.dto.problem.ProblemResponseDto
import com.csbroker.apiserver.dto.problem.ProblemSearchDto
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
        @RequestParam("type", required = false) type: String?,
        @RequestParam("isGradable", required = false) isGradable: Boolean?,
        pageable: Pageable
    ): ApiResponse<List<ProblemResponseDto>> {
        var solvedBy: String? = null

        if (isSolved != null && isSolved) {
            try {
                val principal = SecurityContextHolder.getContext().authentication.principal
                    as org.springframework.security.core.userdetails.User
                solvedBy = principal.username
            } catch (e: Exception) {
                throw UnAuthorizedException(ErrorCode.UNAUTHORIZED, "사용자 권한이 없습니다.")
            }
        }

        val searchDto = ProblemSearchDto(tags, solvedBy, query, type, isGradable)
        val foundProblems = this.problemService.findProblems(searchDto, pageable)

        return ApiResponse.success(foundProblems)
    }

    @GetMapping("/{id}")
    fun getProblemById(@PathVariable("id") id: Long): ApiResponse<ProblemDetailResponseDto> {
        val findProblem = this.problemService.findProblemById(id)
            ?: throw EntityNotFoundException("${id}번 문제를 찾을 수 없습니다.")

        return ApiResponse.success(findProblem)
    }

    @PostMapping("/long/{id}/grade")
    fun gradeLongProblem(
        @LoginUser loginUser: User,
        @PathVariable("id") id: Long,
        @RequestBody longProblemAnswerDto: LongProblemAnswerDto
    ): ApiResponse<LongProblemGradingHistoryDto> {
        val gradeHistory = this.problemService.gradingLongProblem(loginUser.username, id, longProblemAnswerDto.answer)
        return ApiResponse.success(gradeHistory)
    }
}
