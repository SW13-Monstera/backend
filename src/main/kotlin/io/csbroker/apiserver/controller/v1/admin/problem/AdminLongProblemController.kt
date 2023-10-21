package io.csbroker.apiserver.controller.v1.admin.problem

import io.csbroker.apiserver.auth.LoginUser
import io.csbroker.apiserver.dto.common.ApiResponse
import io.csbroker.apiserver.dto.common.UpsertSuccessResponseDto
import io.csbroker.apiserver.dto.problem.AdminProblemSearchDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemResponseDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemSearchResponseDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemUpsertRequestDto
import io.csbroker.apiserver.model.User
import io.csbroker.apiserver.service.problem.admin.AdminLongProblemService
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/problems/long")
class AdminLongProblemController(
    private val longProblemService: AdminLongProblemService,
) {
    @GetMapping("/{id}")
    fun getLongProblem(
        @PathVariable("id") id: Long,
    ): ApiResponse<LongProblemResponseDto> {
        val longProblemResponseDto = longProblemService.findProblemById(id)
        return ApiResponse.success(longProblemResponseDto)
    }

    @PostMapping
    fun createLongProblem(
        @RequestBody createRequestDto: LongProblemUpsertRequestDto,
        @LoginUser loginUser: User,
    ): ApiResponse<UpsertSuccessResponseDto> {
        val createProblemId = longProblemService.createProblem(createRequestDto, loginUser)
        return ApiResponse.success(UpsertSuccessResponseDto(createProblemId))
    }

    @PutMapping("/{id}")
    fun updateLongProblem(
        @PathVariable("id") id: Long,
        @RequestBody updateRequestDto: LongProblemUpsertRequestDto,
        @LoginUser loginUser: User,
    ): ApiResponse<UpsertSuccessResponseDto> {
        val updateProblemId = longProblemService.updateProblem(id, updateRequestDto)
        return ApiResponse.success(UpsertSuccessResponseDto(updateProblemId))
    }

    @GetMapping
    fun findLongProblemsByQuery(
        @RequestParam("id", required = false) id: Long?,
        @RequestParam("title", required = false) title: String?,
        @RequestParam("description", required = false) description: String?,
        pageable: Pageable,
    ): ApiResponse<LongProblemSearchResponseDto> {
        val problemSearchDto = AdminProblemSearchDto(id, title, description, pageable)
        return ApiResponse.success(longProblemService.findProblems(problemSearchDto))
    }
}
