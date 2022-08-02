package com.csbroker.apiserver.controller

import com.csbroker.apiserver.common.auth.LoginUser
import com.csbroker.apiserver.dto.ApiResponse
import com.csbroker.apiserver.dto.UpsertSuccessResponseDto
import com.csbroker.apiserver.dto.UserAnswerBatchInsertDto
import com.csbroker.apiserver.dto.UserAnswerLabelRequestDto
import com.csbroker.apiserver.dto.UserAnswerResponseDto
import com.csbroker.apiserver.dto.UserAnswerSearchResponseDto
import com.csbroker.apiserver.dto.UserAnswerUpsertDto
import com.csbroker.apiserver.dto.problem.LongProblemResponseDto
import com.csbroker.apiserver.dto.problem.LongProblemSearchResponseDto
import com.csbroker.apiserver.dto.problem.LongProblemUpsertRequestDto
import com.csbroker.apiserver.dto.problem.MultipleChoiceProblemSearchResponseDto
import com.csbroker.apiserver.dto.problem.MultipleChoiceProblemUpsertRequestDto
import com.csbroker.apiserver.dto.problem.MultipleProblemResponseDto
import com.csbroker.apiserver.dto.problem.ProblemDeleteRequestDto
import com.csbroker.apiserver.dto.problem.ShortProblemResponseDto
import com.csbroker.apiserver.dto.problem.ShortProblemSearchResponseDto
import com.csbroker.apiserver.dto.problem.ShortProblemUpsertRequestDto
import com.csbroker.apiserver.dto.user.AdminUserInfoResponseDto
import com.csbroker.apiserver.service.ProblemService
import com.csbroker.apiserver.service.UserAnswerService
import com.csbroker.apiserver.service.UserService
import org.springframework.data.domain.Pageable
import org.springframework.security.core.userdetails.User
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin")
class AdminController(
    private val problemService: ProblemService,
    private val userAnswerService: UserAnswerService,
    private val userService: UserService
) {
    @GetMapping("/users/admin")
    fun findAdminUsers(): ApiResponse<List<AdminUserInfoResponseDto>> {
        val adminUserInfoResponseDtoList = this.userService.findAdminUsers().map {
            AdminUserInfoResponseDto(
                it.id!!,
                it.username
            )
        }

        return ApiResponse.success(adminUserInfoResponseDtoList)
    }

    @PostMapping("/problems/long")
    fun createLongProblem(
        @RequestBody createRequestDto: LongProblemUpsertRequestDto,
        @LoginUser loginUser: User
    ): ApiResponse<UpsertSuccessResponseDto> {
        val createProblemId = this.problemService.createLongProblem(createRequestDto, loginUser.username)
        return ApiResponse.success(UpsertSuccessResponseDto(createProblemId))
    }

    @PutMapping("/problems/long/{id}")
    fun updateLongProblem(
        @PathVariable("id") id: Long,
        @RequestBody updateRequestDto: LongProblemUpsertRequestDto,
        @LoginUser loginUser: User
    ): ApiResponse<UpsertSuccessResponseDto> {
        val updateProblemId = this.problemService.updateLongProblem(id, updateRequestDto, loginUser.username)
        return ApiResponse.success(UpsertSuccessResponseDto(updateProblemId))
    }

    @GetMapping("/problems/long/{id}")
    fun getLongProblem(
        @PathVariable("id") id: Long
    ): ApiResponse<LongProblemResponseDto> {
        val longProblemResponseDto = this.problemService.findLongProblemById(id)
        return ApiResponse.success(longProblemResponseDto)
    }

    @GetMapping("/problems/short/{id}")
    fun getShortProblem(
        @PathVariable("id") id: Long
    ): ApiResponse<ShortProblemResponseDto> {
        val shortProblemResponseDto = this.problemService.findShortProblemById(id)
        return ApiResponse.success(shortProblemResponseDto)
    }

    @GetMapping("/problems/multiple/{id}")
    fun getMultipleChoiceProblem(
        @PathVariable("id") id: Long
    ): ApiResponse<MultipleProblemResponseDto> {
        val multipleProblemResponseDto = this.problemService.findMultipleProblemById(id)
        return ApiResponse.success(multipleProblemResponseDto)
    }

    @PostMapping("/problems/short")
    fun createShortProblem(
        @RequestBody createRequestDto: ShortProblemUpsertRequestDto,
        @LoginUser loginUser: User
    ): ApiResponse<UpsertSuccessResponseDto> {
        val createProblemId = this.problemService.createShortProblem(createRequestDto, loginUser.username)
        return ApiResponse.success(UpsertSuccessResponseDto(createProblemId))
    }

    @PutMapping("/problems/short/{id}")
    fun updateLongProblem(
        @PathVariable("id") id: Long,
        @RequestBody updateRequestDto: ShortProblemUpsertRequestDto,
        @LoginUser loginUser: User
    ): ApiResponse<UpsertSuccessResponseDto> {
        val updateProblemId = this.problemService.updateShortProblem(id, updateRequestDto, loginUser.username)
        return ApiResponse.success(UpsertSuccessResponseDto(updateProblemId))
    }

    @PostMapping("/problems/multiple")
    fun createMultipleProblem(
        @RequestBody createRequestDto: MultipleChoiceProblemUpsertRequestDto,
        @LoginUser loginUser: User
    ): ApiResponse<UpsertSuccessResponseDto> {
        val createProblemId = this.problemService.createMultipleChoiceProblem(createRequestDto, loginUser.username)
        return ApiResponse.success(UpsertSuccessResponseDto(createProblemId))
    }

    @PutMapping("/problems/multiple/{id}")
    fun updateMultipleProblem(
        @PathVariable("id") id: Long,
        @RequestBody updateRequestDto: MultipleChoiceProblemUpsertRequestDto,
        @LoginUser loginUser: User
    ): ApiResponse<UpsertSuccessResponseDto> {
        val updateProblemId = this.problemService.updateMultipleChoiceProblem(id, updateRequestDto, loginUser.username)
        return ApiResponse.success(UpsertSuccessResponseDto(updateProblemId))
    }

    @DeleteMapping("/problems/{id}")
    fun deleteProblem(
        @PathVariable("id") id: Long
    ): ApiResponse<Boolean> {
        this.problemService.removeProblemById(id)
        return ApiResponse.success(true)
    }

    @DeleteMapping("/problems")
    fun deleteProblems(
        @RequestBody deleteRequestDto: ProblemDeleteRequestDto
    ): ApiResponse<Boolean> {
        this.problemService.removeProblemsById(deleteRequestDto.ids)
        return ApiResponse.success(true)
    }

    @PostMapping("/user-answers")
    fun createUserAnswers(
        @RequestBody userAnswers: UserAnswerBatchInsertDto
    ): ApiResponse<UpsertSuccessResponseDto> {
        val size = this.userAnswerService.createUserAnswers(userAnswers.userAnswers)
        if (size != userAnswers.size) {
            throw IllegalArgumentException("버그 발생!!!!")
        }
        return ApiResponse.success(UpsertSuccessResponseDto(size = size))
    }

    @PostMapping("/user-answer")
    fun createUserAnswer(
        @RequestBody userAnswer: UserAnswerUpsertDto
    ): ApiResponse<UpsertSuccessResponseDto> {
        val createUserAnswerId = this.userAnswerService.createUserAnswer(userAnswer)
        return ApiResponse.success(UpsertSuccessResponseDto(id = createUserAnswerId))
    }

    @GetMapping("/user-answers/{id}")
    fun getUserAnswerById(
        @PathVariable("id") id: Long
    ): ApiResponse<UserAnswerResponseDto> {
        return ApiResponse.success(this.userAnswerService.findUserAnswerById(id))
    }

    @PostMapping("/user-answers/{id}/{type:label|validate}")
    fun checkUserAnswer(
        @PathVariable("id") id: Long,
        @RequestBody userAnswerLabelRequestDto: UserAnswerLabelRequestDto,
        @PathVariable("type") type: String,
        @LoginUser loginUser: User
    ): ApiResponse<UpsertSuccessResponseDto> {
        val answerId = when (type) {
            "label" ->
                this.userAnswerService.labelUserAnswer(
                    loginUser.username,
                    id,
                    userAnswerLabelRequestDto.selectedGradingStandardIds
                )
            "validate" ->
                this.userAnswerService.validateUserAnswer(
                    loginUser.username,
                    id,
                    userAnswerLabelRequestDto.selectedGradingStandardIds
                )
            else -> throw IllegalArgumentException("존재하지 않는 uri ( $type ) 입니다. ")
        }
        return ApiResponse.success(UpsertSuccessResponseDto(id = answerId))
    }

    @GetMapping("/problems/long")
    fun findLongProblemsByQuery(
        @RequestParam("id", required = false) id: Long?,
        @RequestParam("title", required = false) title: String?,
        @RequestParam("description", required = false) description: String?,
        pageable: Pageable
    ): ApiResponse<LongProblemSearchResponseDto> {
        return ApiResponse.success(this.problemService.findLongProblems(id, title, description, pageable))
    }

    @GetMapping("/problems/short")
    fun findShortProblemsByQuery(
        @RequestParam("id", required = false) id: Long?,
        @RequestParam("title", required = false) title: String?,
        @RequestParam("description", required = false) description: String?,
        pageable: Pageable
    ): ApiResponse<ShortProblemSearchResponseDto> {
        return ApiResponse.success(this.problemService.findShortProblems(id, title, description, pageable))
    }

    @GetMapping("/problems/multiple")
    fun findMultipleChoiceProblemsByQuery(
        @RequestParam("id", required = false) id: Long?,
        @RequestParam("title", required = false) title: String?,
        @RequestParam("description", required = false) description: String?,
        pageable: Pageable
    ): ApiResponse<MultipleChoiceProblemSearchResponseDto> {
        return ApiResponse.success(this.problemService.findMultipleProblems(id, title, description, pageable))
    }

    @GetMapping("/user-answers")
    fun findUserAnswersByQuery(
        @RequestParam("id", required = false) id: Long?,
        @RequestParam("assignedBy", required = false) assignedBy: String?,
        @RequestParam("validatedBy", required = false) validatedBy: String?,
        @RequestParam("problemTitle", required = false) problemTitle: String?,
        @RequestParam("answer", required = false) answer: String?,
        @RequestParam("isLabeled", required = false) isLabeled: Boolean?,
        @RequestParam("isValidated", required = false) isValidated: Boolean?,
        pageable: Pageable
    ): ApiResponse<UserAnswerSearchResponseDto> {
        return ApiResponse.success(
            this.userAnswerService.findUserAnswersByQuery(
                id,
                assignedBy,
                validatedBy,
                problemTitle,
                answer,
                isLabeled,
                isValidated,
                pageable
            )
        )
    }
}
