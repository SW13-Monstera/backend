package io.csbroker.apiserver.controller.v1.problem

import io.csbroker.apiserver.auth.LoginUser
import io.csbroker.apiserver.common.util.getEmailFromSecurityContextHolder
import io.csbroker.apiserver.dto.common.ApiResponse
import io.csbroker.apiserver.dto.problem.grade.MultipleProblemGradingRequestDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemAnswerDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemDetailResponseDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemGradingHistoryDto
import io.csbroker.apiserver.service.problem.MultipleProblemService
import io.csbroker.apiserver.model.User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/problems/multiple")
class MultipleProblemController(
    private val multipleProblemService: MultipleProblemService,
) {
    @GetMapping("/{id}")
    fun getMultipleProblemById(@PathVariable("id") id: Long): ApiResponse<MultipleChoiceProblemDetailResponseDto> {
        val findProblemDetail =
            multipleProblemService.findProblemById(id, getEmailFromSecurityContextHolder())

        return ApiResponse.success(findProblemDetail)
    }

    @PostMapping("/{id}/grade")
    fun gradeMultipleChoiceProblem(
        @LoginUser loginUser: User,
        @PathVariable("id") id: Long,
        @RequestBody answerDto: MultipleChoiceProblemAnswerDto,
    ): ApiResponse<MultipleChoiceProblemGradingHistoryDto> {
        val gradingRequestDto = MultipleProblemGradingRequestDto(loginUser, id, answerDto.answerIds)
        val gradeHistory = multipleProblemService.gradingProblem(gradingRequestDto)
        return ApiResponse.success(gradeHistory)
    }
}
