package com.csbroker.apiserver.controller

import com.csbroker.apiserver.common.auth.LoginUser
import com.csbroker.apiserver.dto.ApiResponse
import com.csbroker.apiserver.dto.CreateSuccessResponseDto
import com.csbroker.apiserver.dto.problem.LongProblemCreateRequestDto
import com.csbroker.apiserver.dto.problem.MultipleChoiceProblemCreateRequestDto
import com.csbroker.apiserver.dto.problem.ShortProblemCreateRequestDto
import com.csbroker.apiserver.service.ProblemService
import org.springframework.security.core.userdetails.User
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin")
class AdminController(
    private val problemService: ProblemService
) {
    @PostMapping("/problems/long")
    fun createLongProblem(
        @RequestBody createRequestDto: LongProblemCreateRequestDto,
        @LoginUser loginUser: User
    ): ApiResponse<CreateSuccessResponseDto> {
        val createProblemId = this.problemService.createLongProblem(createRequestDto, loginUser.username)
        return ApiResponse.success(CreateSuccessResponseDto(createProblemId))
    }

    @PostMapping("/problems/short")
    fun createShortProblem(
        @RequestBody createRequestDto: ShortProblemCreateRequestDto,
        @LoginUser loginUser: User
    ): ApiResponse<CreateSuccessResponseDto> {
        val createProblemId = this.problemService.createShortProblem(createRequestDto, loginUser.username)
        return ApiResponse.success(CreateSuccessResponseDto(createProblemId))
    }

    @PostMapping("/problems/multiple")
    fun createMultipleProblem(
        @RequestBody createRequestDto: MultipleChoiceProblemCreateRequestDto,
        @LoginUser loginUser: User
    ): ApiResponse<CreateSuccessResponseDto> {
        val createProblemId = this.problemService.createMultipleChoiceProblem(createRequestDto, loginUser.username)
        return ApiResponse.success(CreateSuccessResponseDto(createProblemId))
    }
}
