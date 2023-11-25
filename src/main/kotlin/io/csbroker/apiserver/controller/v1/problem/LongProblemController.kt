package io.csbroker.apiserver.controller.v1.problem

import io.csbroker.apiserver.auth.LoginUser
import io.csbroker.apiserver.common.util.getEmailFromSecurityContextHolder
import io.csbroker.apiserver.controller.v2.problem.request.SubmitLongProblemDto
import io.csbroker.apiserver.controller.v2.problem.response.SubmitLongProblemResponseDto
import io.csbroker.apiserver.dto.common.ApiResponse
import io.csbroker.apiserver.dto.problem.grade.LongProblemGradingRequestDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemAnswerDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemDetailResponseDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemGradingHistoryDto
import io.csbroker.apiserver.model.User
import io.csbroker.apiserver.service.post.PostService
import io.csbroker.apiserver.service.problem.LongProblemService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/problems/long")
class LongProblemController(
    private val longProblemService: LongProblemService,
    private val postService: PostService,
) {
    @GetMapping("/{id}")
    fun getLongProblemById(@PathVariable("id") id: Long): ApiResponse<LongProblemDetailResponseDto> {
        val findProblemDetail = longProblemService.findProblemById(id, getEmailFromSecurityContextHolder())

        return ApiResponse.success(findProblemDetail)
    }

    @PostMapping("/{id}/grade")
    fun gradeLongProblem(
        @LoginUser loginUser: User,
        @PathVariable("id") id: Long,
        @RequestBody answerDto: LongProblemAnswerDto,
        @RequestParam("isGrading", required = false) isGrading: Boolean?,
    ): ApiResponse<LongProblemGradingHistoryDto> {
        val gradingRequestDto = LongProblemGradingRequestDto(
            loginUser.email,
            id,
            answerDto.answer,
            isGrading ?: true,
        )
        val gradeHistory = longProblemService.gradingProblem(gradingRequestDto)
        return ApiResponse.success(gradeHistory)
    }

    @PostMapping("{id}/submit")
    fun submitLongProblem(
        @LoginUser loginUser: User,
        @PathVariable("id") problemId: Long,
        @RequestBody answerDto: LongProblemAnswerDto,
    ): ApiResponse<SubmitLongProblemResponseDto> {
        val submitRequestDto = SubmitLongProblemDto(loginUser, problemId, answerDto.answer)
        val submitResponseDto = longProblemService.submitProblem(submitRequestDto)
        postService.create(submitRequestDto.problemId, answerDto.answer, loginUser) // Todo : LongProblemService 내부로 보내기
        return ApiResponse.success(submitResponseDto)
    }
}
