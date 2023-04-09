package io.csbroker.apiserver.controller.v1.admin.problem

import io.csbroker.apiserver.dto.common.ApiResponse
import io.csbroker.apiserver.dto.problem.ProblemDeleteRequestDto
import io.csbroker.apiserver.service.problem.CommonProblemService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin")
class AdminProblemController(
    private val problemService: CommonProblemService,
) {
    @DeleteMapping("/problems/{id}")
    fun deleteProblem(
        @PathVariable("id") id: Long,
    ): ApiResponse<Boolean> {
        problemService.removeProblemById(id)
        return ApiResponse.success(true)
    }

    @DeleteMapping("/problems")
    fun deleteProblems(
        @RequestBody deleteRequestDto: ProblemDeleteRequestDto,
    ): ApiResponse<Boolean> {
        problemService.removeProblemsById(deleteRequestDto.ids)
        return ApiResponse.success(true)
    }

}
