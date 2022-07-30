package com.csbroker.apiserver.controller

import com.csbroker.apiserver.common.auth.LoginUser
import com.csbroker.apiserver.dto.ApiResponse
import com.csbroker.apiserver.dto.UpsertSuccessResponseDto
import com.csbroker.apiserver.dto.UserAnswerBatchInsertDto
import com.csbroker.apiserver.dto.problem.LongProblemUpsertRequestDto
import com.csbroker.apiserver.dto.problem.MultipleChoiceProblemUpsertRequestDto
import com.csbroker.apiserver.dto.problem.ProblemDeleteRequestDto
import com.csbroker.apiserver.dto.problem.ShortProblemUpsertRequestDto
import com.csbroker.apiserver.service.ProblemService
import com.csbroker.apiserver.service.UserAnswerService
import org.springframework.security.core.userdetails.User
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin")
class AdminController(
    private val problemService: ProblemService,
    private val userAnswerService: UserAnswerService
) {
    @PostMapping("/problems/long")
    fun createLongProblem(
        @RequestBody createRequestDto: LongProblemUpsertRequestDto,
        @LoginUser loginUser: User
    ): ApiResponse<UpsertSuccessResponseDto> {
        val createProblemId = this.problemService.createLongProblem(createRequestDto, loginUser.username)
        return ApiResponse.success(UpsertSuccessResponseDto(createProblemId))
    }

    @PutMapping("/problems/long/{id}")
    fun updateLongProblem(
        @PathVariable("id") id: Long,
        @RequestBody updateRequestDto: LongProblemUpsertRequestDto,
        @LoginUser loginUser: User
    ): ApiResponse<UpsertSuccessResponseDto> {
        val updateProblemId = this.problemService.updateLongProblem(id, updateRequestDto, loginUser.username)
        return ApiResponse.success(UpsertSuccessResponseDto(updateProblemId))
    }

    @PostMapping("/problems/short")
    fun createShortProblem(
        @RequestBody createRequestDto: ShortProblemUpsertRequestDto,
        @LoginUser loginUser: User
    ): ApiResponse<UpsertSuccessResponseDto> {
        val createProblemId = this.problemService.createShortProblem(createRequestDto, loginUser.username)
        return ApiResponse.success(UpsertSuccessResponseDto(createProblemId))
    }

    @PutMapping("/problems/short/{id}")
    fun updateLongProblem(
        @PathVariable("id") id: Long,
        @RequestBody updateRequestDto: ShortProblemUpsertRequestDto,
        @LoginUser loginUser: User
    ): ApiResponse<UpsertSuccessResponseDto> {
        val updateProblemId = this.problemService.updateShortProblem(id, updateRequestDto, loginUser.username)
        return ApiResponse.success(UpsertSuccessResponseDto(updateProblemId))
    }

    @PostMapping("/problems/multiple")
    fun createMultipleProblem(
        @RequestBody createRequestDto: MultipleChoiceProblemUpsertRequestDto,
        @LoginUser loginUser: User
    ): ApiResponse<UpsertSuccessResponseDto> {
        val createProblemId = this.problemService.createMultipleChoiceProblem(createRequestDto, loginUser.username)
        return ApiResponse.success(UpsertSuccessResponseDto(createProblemId))
    }

    @PutMapping("/problems/multiple/{id}")
    fun updateMultipleProblem(
        @PathVariable("id") id: Long,
        @RequestBody updateRequestDto: MultipleChoiceProblemUpsertRequestDto,
        @LoginUser loginUser: User
    ): ApiResponse<UpsertSuccessResponseDto> {
        val updateProblemId = this.problemService.updateMultipleChoiceProblem(id, updateRequestDto, loginUser.username)
        return ApiResponse.success(UpsertSuccessResponseDto(updateProblemId))
    }

    @DeleteMapping("/problems/{id}")
    fun deleteProblem(
        @PathVariable("id") id: Long
    ): ApiResponse<Boolean> {
        this.problemService.removeProblemById(id)
        return ApiResponse.success(true)
    }

    @DeleteMapping("/problems")
    fun deleteProblems(
        @RequestBody deleteRequestDto: ProblemDeleteRequestDto
    ): ApiResponse<Boolean> {
        this.problemService.removeProblemsById(deleteRequestDto.ids)
        return ApiResponse.success(true)
    }

    @PostMapping("/user-answers")
    fun crateUserAnswers(
        @RequestBody userAnswers: UserAnswerBatchInsertDto,
        @LoginUser loginUser: User
    ): ApiResponse<UpsertSuccessResponseDto> {
        val size = this.userAnswerService.createUserAnswers(userAnswers.userAnswers)
        if (size != userAnswers.size) {
            throw IllegalArgumentException("버그 발생!!!!")
        }
        return ApiResponse.success(UpsertSuccessResponseDto(size = size))
    }
}
