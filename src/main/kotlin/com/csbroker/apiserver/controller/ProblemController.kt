package com.csbroker.apiserver.controller

import com.csbroker.apiserver.dto.ApiResponse
import com.csbroker.apiserver.dto.ProblemDetailResponseDto
import com.csbroker.apiserver.dto.ProblemResponseDto
import com.csbroker.apiserver.dto.ProblemSearchDto
import com.csbroker.apiserver.service.ProblemService
import org.springframework.data.domain.Pageable
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

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
        var solvedBy: String? = null

        if (isSolved) {
            try {
                val principal = SecurityContextHolder.getContext().authentication.principal
                    as org.springframework.security.core.userdetails.User
                solvedBy = principal.username
            } catch (e: Exception) {
                throw IllegalArgumentException("사용자 권한이 없습니다.")
            }
        }

        val searchDto = ProblemSearchDto(tags, solvedBy, query)
        val foundProblems = this.problemService.findProblems(searchDto, pageable)

        return ApiResponse.success(foundProblems)
    }

    @GetMapping("/{id}")
    fun getProblemById(@PathVariable("id") id: Long): ApiResponse<ProblemDetailResponseDto> {
        val findProblem = this.problemService.findProblemById(id)
            ?: throw IllegalArgumentException("$id is not appropriate id")

        return ApiResponse.success(findProblem)
    }
}
