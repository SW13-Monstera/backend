package io.csbroker.apiserver.controller.v1.problem

import io.csbroker.apiserver.auth.LoginUser
import io.csbroker.apiserver.common.util.getEmailFromSecurityContextHolder
import io.csbroker.apiserver.dto.common.ApiResponse
import io.csbroker.apiserver.dto.problem.grade.ShortProblemGradingRequestDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemAnswerDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemDetailResponseDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemGradingHistoryDto
import io.csbroker.apiserver.service.problem.ShortProblemService
import io.csbroker.apiserver.model.User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/problems/short")
class ShortProblemController(
    private val shortProblemService: ShortProblemService,
) {
    @GetMapping("/{id}")
    fun getShortProblemById(@PathVariable("id") id: Long): ApiResponse<ShortProblemDetailResponseDto> {
        val findProblemDetail = shortProblemService.findProblemById(id, getEmailFromSecurityContextHolder())

        return ApiResponse.success(findProblemDetail)
    }

    @PostMapping("/{id}/grade")
    fun gradeShortProblem(
        @LoginUser loginUser: User,
        @PathVariable("id") id: Long,
        @RequestBody answerDto: ShortProblemAnswerDto,
    ): ApiResponse<ShortProblemGradingHistoryDto> {
        val gradingRequestDto = ShortProblemGradingRequestDto(loginUser, id, answerDto.answer)
        val gradeHistory = shortProblemService.gradingProblem(gradingRequestDto)
        return ApiResponse.success(gradeHistory)
    }
}
