package io.csbroker.apiserver.controller.v2.problem

import io.csbroker.apiserver.auth.LoginUser
import io.csbroker.apiserver.controller.v2.problem.request.ChallengeCreateRequest
import io.csbroker.apiserver.dto.common.ApiResponse
import io.csbroker.apiserver.dto.problem.challenge.CreateChallengeDto
import io.csbroker.apiserver.service.problem.CommonProblemService
import org.springframework.security.core.userdetails.User
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/api/v2/problems")
class ProblemControllerV2(
    private val commonProblemService: CommonProblemService,
) {
    @PostMapping("/{id}/challenge")
    fun challengeProblem(
        @LoginUser loginUser: User,
        @PathVariable("id") id: Long,
        @Valid
        @RequestBody
        challengeCreateRequest: ChallengeCreateRequest,
    ): ApiResponse<Boolean> {
        commonProblemService.createChallenge(CreateChallengeDto(loginUser.username, id, challengeCreateRequest.content))
        return ApiResponse.success(true)
    }
}
