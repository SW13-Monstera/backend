package io.csbroker.apiserver.controller.v1.admin.problem

import io.csbroker.apiserver.dto.common.ApiResponse
import io.csbroker.apiserver.dto.problem.problemset.ProblemSetUpsertRequestDto
import io.csbroker.apiserver.service.problem.ProblemSetService
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/problem-sets")
class AdminProblemSetController(private val problemSetService: ProblemSetService) {

    @PostMapping
    fun createProblemSet(
        @RequestBody problemSetUpsertRequestDto: ProblemSetUpsertRequestDto,
    ): ApiResponse<Long> {
        return ApiResponse.success(problemSetService.createProblemSet(problemSetUpsertRequestDto))
    }

    @PutMapping("/{id}")
    fun updateProblemSet(
        @PathVariable("id") id: Long,
        @RequestBody problemSetUpsertRequestDto: ProblemSetUpsertRequestDto,
    ): ApiResponse<Long> {
        return ApiResponse.success(problemSetService.updateProblemSet(id, problemSetUpsertRequestDto))
    }
}
