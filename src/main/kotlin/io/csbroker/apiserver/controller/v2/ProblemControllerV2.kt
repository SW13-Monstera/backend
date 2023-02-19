package io.csbroker.apiserver.controller.v2

import io.csbroker.apiserver.common.util.getEmailFromSecurityContextHolder
import io.csbroker.apiserver.controller.v2.response.ShortProblemDetailResponseV2Dto
import io.csbroker.apiserver.dto.common.ApiResponse
import io.csbroker.apiserver.service.ProblemService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v2/problems")
class ProblemControllerV2(
    private val problemService: ProblemService,
) {

    @GetMapping("/short/{id}")
    fun getShortProblemById(@PathVariable("id") id: Long): ApiResponse<ShortProblemDetailResponseV2Dto> {
        val findProblemDetail = problemService.findShortProblemDetailByIdV2(
            id,
            getEmailFromSecurityContextHolder(),
        )

        return ApiResponse.success(findProblemDetail)
    }
}
