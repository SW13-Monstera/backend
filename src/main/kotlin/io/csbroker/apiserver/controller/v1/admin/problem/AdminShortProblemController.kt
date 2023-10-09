package io.csbroker.apiserver.controller.v1.admin.problem

import io.csbroker.apiserver.auth.LoginUser
import io.csbroker.apiserver.dto.common.ApiResponse
import io.csbroker.apiserver.dto.common.UpsertSuccessResponseDto
import io.csbroker.apiserver.dto.problem.AdminProblemSearchDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemResponseDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemSearchResponseDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemUpsertRequestDto
import io.csbroker.apiserver.service.problem.admin.AdminShortProblemService
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
@RequestMapping("/api/admin/problems/short")
class AdminShortProblemController(
    private val shortProblemService: AdminShortProblemService,
) {
    @GetMapping("/{id}")
    fun getShortProblem(
        @PathVariable("id") id: Long,
    ): ApiResponse<ShortProblemResponseDto> {
        val shortProblemResponseDto = shortProblemService.findProblemById(id)
        return ApiResponse.success(shortProblemResponseDto)
    }

    @PostMapping
    fun createShortProblem(
        @RequestBody createRequestDto: ShortProblemUpsertRequestDto,
        @LoginUser loginUser: User,
    ): ApiResponse<UpsertSuccessResponseDto> {
        val createProblemId = shortProblemService.createProblem(createRequestDto, loginUser)
        return ApiResponse.success(UpsertSuccessResponseDto(createProblemId))
    }

    @PutMapping("/{id}")
    fun updateLongProblem(
        @PathVariable("id") id: Long,
        @RequestBody updateRequestDto: ShortProblemUpsertRequestDto,
        @LoginUser loginUser: User,
    ): ApiResponse<UpsertSuccessResponseDto> {
        val updateProblemId = shortProblemService.updateProblem(id, updateRequestDto)
        return ApiResponse.success(UpsertSuccessResponseDto(updateProblemId))
    }

    @GetMapping
    fun findShortProblemsByQuery(
        @RequestParam("id", required = false) id: Long?,
        @RequestParam("title", required = false) title: String?,
        @RequestParam("description", required = false) description: String?,
        pageable: Pageable,
    ): ApiResponse<ShortProblemSearchResponseDto> {
        val problemSearchDto = AdminProblemSearchDto(id, title, description, pageable)
        return ApiResponse.success(shortProblemService.findProblems(problemSearchDto))
    }
}
