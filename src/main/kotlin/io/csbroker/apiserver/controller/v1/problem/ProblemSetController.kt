package io.csbroker.apiserver.controller.v1.problem

import io.csbroker.apiserver.dto.common.ApiResponse
import io.csbroker.apiserver.dto.problem.problemset.ProblemSetDetailResponseDto
import io.csbroker.apiserver.dto.problem.problemset.ProblemSetResponseDto
import io.csbroker.apiserver.service.problem.ProblemSetService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/problem-sets")
class ProblemSetController(
    private val problemSetService: ProblemSetService,
) {
    @GetMapping("/{id}")
    fun findById(
        @PathVariable("id") id: Long,
    ): ApiResponse<ProblemSetDetailResponseDto> {
        return ApiResponse.success(problemSetService.findById(id).toProblemSetDetailResponseDto())
    }

    @GetMapping
    fun findAll(): ApiResponse<List<ProblemSetResponseDto>> {
        return ApiResponse.success(problemSetService.findAll().map { it.toProblemSetResponseDto() })
    }
}
