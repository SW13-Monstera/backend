package com.csbroker.apiserver.controller

import com.csbroker.apiserver.dto.ApiResponse
import com.csbroker.apiserver.dto.ProblemDetailResponseDto
import com.csbroker.apiserver.dto.ProblemResponseDto
import com.csbroker.apiserver.dto.ProblemSearchDto
import com.csbroker.apiserver.service.ProblemService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/problems")
class ProblemController {

    @Autowired
    private lateinit var problemService: ProblemService

    @GetMapping("/")
    fun getAllProblemsByQuery(
        @RequestParam("query") query: String,
        @RequestParam("isSolved") isSolved: Boolean,
        @RequestParam("tags") tags: List<String>
    ): ApiResponse<List<ProblemResponseDto>> {
        // TODO("Auth 권한 체크하여, solved, not solved 체크")
        val searchDto = ProblemSearchDto(tags, null, query)
        val foundProblems = this.problemService.findProblems(searchDto)

        return ApiResponse.success(foundProblems)
    }

    @GetMapping("/{id}")
    fun getProblemById(@PathVariable("id") id: String): ApiResponse<ProblemDetailResponseDto?> {
        val uuid = UUID.fromString(id)
        val findProblem = this.problemService.findProblemById(uuid)
        return ApiResponse.success(findProblem)
    }
}
