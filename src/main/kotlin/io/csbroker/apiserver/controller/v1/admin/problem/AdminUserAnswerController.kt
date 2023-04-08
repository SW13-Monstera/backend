package io.csbroker.apiserver.controller.v1.admin.problem

import io.csbroker.apiserver.auth.LoginUser
import io.csbroker.apiserver.common.enums.ErrorCode
import io.csbroker.apiserver.common.exception.ConditionConflictException
import io.csbroker.apiserver.dto.common.ApiResponse
import io.csbroker.apiserver.dto.common.UpsertSuccessResponseDto
import io.csbroker.apiserver.dto.useranswer.AssignUserAnswerDto
import io.csbroker.apiserver.dto.useranswer.UserAnswerBatchInsertDto
import io.csbroker.apiserver.dto.useranswer.UserAnswerLabelRequestDto
import io.csbroker.apiserver.dto.useranswer.UserAnswerResponseDto
import io.csbroker.apiserver.dto.useranswer.UserAnswerSearchResponseDto
import io.csbroker.apiserver.dto.useranswer.UserAnswerUpsertDto
import io.csbroker.apiserver.service.problem.UserAnswerService
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
class AdminUserAnswerController(
    private val userAnswerService: UserAnswerService,
) {
    @PostMapping("/user-answers")
    fun createUserAnswers(
        @RequestBody userAnswers: UserAnswerBatchInsertDto,
    ): ApiResponse<UpsertSuccessResponseDto> {
        val size = userAnswerService.createUserAnswers(userAnswers.userAnswers)
        return ApiResponse.success(UpsertSuccessResponseDto(size = size))
    }

    @PostMapping("/user-answer")
    fun createUserAnswer(
        @RequestBody userAnswer: UserAnswerUpsertDto,
    ): ApiResponse<UpsertSuccessResponseDto> {
        val createUserAnswerId = userAnswerService.createUserAnswer(userAnswer)
        return ApiResponse.success(UpsertSuccessResponseDto(id = createUserAnswerId))
    }

    @GetMapping("/user-answers/{id}")
    fun getUserAnswerById(
        @PathVariable("id") id: Long,
    ): ApiResponse<UserAnswerResponseDto> {
        return ApiResponse.success(userAnswerService.findUserAnswerById(id))
    }

    @PostMapping("/user-answers/{id}/{type:label|validate}")
    fun checkUserAnswer(
        @PathVariable("id") id: Long,
        @RequestBody userAnswerLabelRequestDto: UserAnswerLabelRequestDto,
        @PathVariable("type") type: String,
        @LoginUser loginUser: User,
    ): ApiResponse<UpsertSuccessResponseDto> {
        val answerId = when (type) {
            "label" ->
                userAnswerService.labelUserAnswer(
                    loginUser.username,
                    id,
                    userAnswerLabelRequestDto.selectedGradingStandardIds,
                )

            "validate" ->
                userAnswerService.validateUserAnswer(
                    loginUser.username,
                    id,
                    userAnswerLabelRequestDto.selectedGradingStandardIds,
                )

            else -> throw ConditionConflictException(
                ErrorCode.CONDITION_NOT_FULFILLED,
                "존재하지 않는 uri ( $type ) 입니다.",
            )
        }
        return ApiResponse.success(UpsertSuccessResponseDto(id = answerId))
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
        pageable: Pageable,
    ): ApiResponse<UserAnswerSearchResponseDto> {
        return ApiResponse.success(
            userAnswerService.findUserAnswersByQuery(
                id,
                assignedBy,
                validatedBy,
                problemTitle,
                answer,
                isLabeled,
                isValidated,
                pageable,
            ),
        )
    }

    @DeleteMapping("/user-answers/{id}")
    fun deleteUserAnswerById(
        @PathVariable("id") id: Long,
    ): ApiResponse<Boolean> {
        userAnswerService.removeUserAnswerById(id)
        return ApiResponse.success(true)
    }

    @PutMapping("/user-answers/assign/{type:label|validate}")
    fun assignUserAnswer(
        @PathVariable("type") type: String,
        @RequestBody assignUserAnswerDto: AssignUserAnswerDto,
    ): ApiResponse<UpsertSuccessResponseDto> {
        if (assignUserAnswerDto.userAnswerIds.isEmpty()) {
            throw ConditionConflictException(ErrorCode.CONDITION_NOT_FULFILLED, "할당 할 user answer가 없습니다.")
        }

        when (type) {
            "label" ->
                userAnswerService.assignLabelUserAnswer(
                    assignUserAnswerDto.userAnswerIds,
                    assignUserAnswerDto.assigneeId,
                )

            "validate" ->
                userAnswerService.assignValidationUserAnswer(
                    assignUserAnswerDto.userAnswerIds,
                    assignUserAnswerDto.assigneeId,
                )

            else -> throw ConditionConflictException(
                ErrorCode.CONDITION_NOT_FULFILLED,
                "존재하지 않는 uri ( $type ) 입니다.",
            )
        }

        return ApiResponse.success(UpsertSuccessResponseDto(size = assignUserAnswerDto.userAnswerIds.size))
    }
}
