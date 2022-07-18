package com.csbroker.apiserver.controller

import com.csbroker.apiserver.dto.ApiResponse
import com.csbroker.apiserver.dto.ProblemDetailResponseDto
import com.csbroker.apiserver.dto.ProblemResponseDto
import com.csbroker.apiserver.dto.ProblemSearchDto
import com.csbroker.apiserver.service.ProblemService
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/problems")
class ProblemController(
    private val problemService: ProblemService
) {
    @GetMapping
    fun getAllProblemsByQuery(
        @RequestParam("query", required = false, defaultValue = "") query: String,
        @RequestParam("isSolved", required = false, defaultValue = "false") isSolved: Boolean,
        @RequestParam("tags", required = false, defaultValue = "") tags: List<String>,
        pageable: Pageable
    ): ApiResponse<List<ProblemResponseDto>> {
        // TODO("Auth 권한 체크하여, solved, not solved 체크")
        val searchDto = ProblemSearchDto(tags, null, query)
        val foundProblems = this.problemService.findProblems(searchDto, pageable)

        return ApiResponse.success(foundProblems)
    }

    @GetMapping("/{id}")
    fun getProblemById(@PathVariable("id") id: UUID): ApiResponse<ProblemDetailResponseDto> {
        val findProblem = this.problemService.findProblemById(id)
            ?: throw IllegalArgumentException("$id is not appropriate id")

        return ApiResponse.success(findProblem)
    }
}
