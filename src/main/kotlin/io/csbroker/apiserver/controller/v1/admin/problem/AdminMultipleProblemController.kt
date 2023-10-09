package io.csbroker.apiserver.controller.v1.admin.problem

import io.csbroker.apiserver.auth.LoginUser
import io.csbroker.apiserver.dto.common.ApiResponse
import io.csbroker.apiserver.dto.common.UpsertSuccessResponseDto
import io.csbroker.apiserver.dto.problem.AdminProblemSearchDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemResponseDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemSearchResponseDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemUpsertRequestDto
import io.csbroker.apiserver.service.problem.admin.AdminMultipleProblemService
import org.springframework.data.domain.Pageable
import io.csbroker.apiserver.model.User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/problems/multiple")
class AdminMultipleProblemController(
    private val multipleProblemService: AdminMultipleProblemService,
) {
    @GetMapping("/{id}")
    fun getMultipleChoiceProblem(
        @PathVariable("id") id: Long,
    ): ApiResponse<MultipleChoiceProblemResponseDto> {
        val multipleProblemResponseDto = multipleProblemService.findProblemById(id)
        return ApiResponse.success(multipleProblemResponseDto)
    }

    @PostMapping
    fun createMultipleProblem(
        @RequestBody createRequestDto: MultipleChoiceProblemUpsertRequestDto,
        @LoginUser loginUser: User,
    ): ApiResponse<UpsertSuccessResponseDto> {
        val createProblemId = multipleProblemService.createProblem(createRequestDto, loginUser)
        return ApiResponse.success(UpsertSuccessResponseDto(createProblemId))
    }

    @PutMapping("/{id}")
    fun updateMultipleProblem(
        @PathVariable("id") id: Long,
        @RequestBody updateRequestDto: MultipleChoiceProblemUpsertRequestDto,
        @LoginUser loginUser: User,
    ): ApiResponse<UpsertSuccessResponseDto> {
        val updateProblemId = multipleProblemService.updateProblem(id, updateRequestDto)
        return ApiResponse.success(UpsertSuccessResponseDto(updateProblemId))
    }

    @GetMapping
    fun findMultipleChoiceProblemsByQuery(
        @RequestParam("id", required = false) id: Long?,
        @RequestParam("title", required = false) title: String?,
        @RequestParam("description", required = false) description: String?,
        pageable: Pageable,
    ): ApiResponse<MultipleChoiceProblemSearchResponseDto> {
        val problemSearchDto = AdminProblemSearchDto(id, title, description, pageable)
        return ApiResponse.success(
            multipleProblemService.findProblems(problemSearchDto),
        )
    }
}
