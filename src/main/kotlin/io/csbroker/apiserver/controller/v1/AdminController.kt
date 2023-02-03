package io.csbroker.apiserver.controller.v1

import io.csbroker.apiserver.auth.LoginUser
import io.csbroker.apiserver.common.enums.ErrorCode
import io.csbroker.apiserver.common.exception.ConditionConflictException
import io.csbroker.apiserver.dto.common.ApiResponse
import io.csbroker.apiserver.dto.common.UpsertSuccessResponseDto
import io.csbroker.apiserver.dto.notification.NotificationBulkInsertDto
import io.csbroker.apiserver.dto.notification.NotificationRequestDto
import io.csbroker.apiserver.dto.problem.ProblemDeleteRequestDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemResponseDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemSearchResponseDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemUpsertRequestDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemResponseDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemSearchResponseDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemUpsertRequestDto
import io.csbroker.apiserver.dto.problem.problemset.ProblemSetUpsertRequestDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemResponseDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemSearchResponseDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemUpsertRequestDto
import io.csbroker.apiserver.dto.user.AdminUserInfoResponseDto
import io.csbroker.apiserver.dto.useranswer.AssignUserAnswerDto
import io.csbroker.apiserver.dto.useranswer.UserAnswerBatchInsertDto
import io.csbroker.apiserver.dto.useranswer.UserAnswerLabelRequestDto
import io.csbroker.apiserver.dto.useranswer.UserAnswerResponseDto
import io.csbroker.apiserver.dto.useranswer.UserAnswerSearchResponseDto
import io.csbroker.apiserver.dto.useranswer.UserAnswerUpsertDto
import io.csbroker.apiserver.service.NotificationService
import io.csbroker.apiserver.service.ProblemService
import io.csbroker.apiserver.service.ProblemSetService
import io.csbroker.apiserver.service.UserAnswerService
import io.csbroker.apiserver.service.UserService
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
    private val userService: UserService,
    private val notificationService: NotificationService,
    private val problemSetService: ProblemSetService
) {
    @GetMapping("/users/admin")
    fun findAdminUsers(): ApiResponse<List<AdminUserInfoResponseDto>> {
        val adminUserInfoResponseDtoList = userService.findAdminUsers().map {
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
        val createProblemId = problemService.createLongProblem(createRequestDto, loginUser.username)
        return ApiResponse.success(UpsertSuccessResponseDto(createProblemId))
    }

    @PutMapping("/problems/long/{id}")
    fun updateLongProblem(
        @PathVariable("id") id: Long,
        @RequestBody updateRequestDto: LongProblemUpsertRequestDto,
        @LoginUser loginUser: User
    ): ApiResponse<UpsertSuccessResponseDto> {
        val updateProblemId = problemService.updateLongProblem(id, updateRequestDto, loginUser.username)
        return ApiResponse.success(UpsertSuccessResponseDto(updateProblemId))
    }

    @GetMapping("/problems/long/{id}")
    fun getLongProblem(
        @PathVariable("id") id: Long
    ): ApiResponse<LongProblemResponseDto> {
        val longProblemResponseDto = problemService.findLongProblemById(id)
        return ApiResponse.success(longProblemResponseDto)
    }

    @GetMapping("/problems/short/{id}")
    fun getShortProblem(
        @PathVariable("id") id: Long
    ): ApiResponse<ShortProblemResponseDto> {
        val shortProblemResponseDto = problemService.findShortProblemById(id)
        return ApiResponse.success(shortProblemResponseDto)
    }

    @GetMapping("/problems/multiple/{id}")
    fun getMultipleChoiceProblem(
        @PathVariable("id") id: Long
    ): ApiResponse<MultipleChoiceProblemResponseDto> {
        val multipleProblemResponseDto = problemService.findMultipleProblemById(id)
        return ApiResponse.success(multipleProblemResponseDto)
    }

    @PostMapping("/problems/short")
    fun createShortProblem(
        @RequestBody createRequestDto: ShortProblemUpsertRequestDto,
        @LoginUser loginUser: User
    ): ApiResponse<UpsertSuccessResponseDto> {
        val createProblemId = problemService.createShortProblem(createRequestDto, loginUser.username)
        return ApiResponse.success(UpsertSuccessResponseDto(createProblemId))
    }

    @PutMapping("/problems/short/{id}")
    fun updateLongProblem(
        @PathVariable("id") id: Long,
        @RequestBody updateRequestDto: ShortProblemUpsertRequestDto,
        @LoginUser loginUser: User
    ): ApiResponse<UpsertSuccessResponseDto> {
        val updateProblemId = problemService.updateShortProblem(id, updateRequestDto, loginUser.username)
        return ApiResponse.success(UpsertSuccessResponseDto(updateProblemId))
    }

    @PostMapping("/problems/multiple")
    fun createMultipleProblem(
        @RequestBody createRequestDto: MultipleChoiceProblemUpsertRequestDto,
        @LoginUser loginUser: User
    ): ApiResponse<UpsertSuccessResponseDto> {
        val createProblemId = problemService.createMultipleChoiceProblem(createRequestDto, loginUser.username)
        return ApiResponse.success(UpsertSuccessResponseDto(createProblemId))
    }

    @PutMapping("/problems/multiple/{id}")
    fun updateMultipleProblem(
        @PathVariable("id") id: Long,
        @RequestBody updateRequestDto: MultipleChoiceProblemUpsertRequestDto,
        @LoginUser loginUser: User
    ): ApiResponse<UpsertSuccessResponseDto> {
        val updateProblemId = problemService.updateMultipleChoiceProblem(id, updateRequestDto, loginUser.username)
        return ApiResponse.success(UpsertSuccessResponseDto(updateProblemId))
    }

    @DeleteMapping("/problems/{id}")
    fun deleteProblem(
        @PathVariable("id") id: Long
    ): ApiResponse<Boolean> {
        problemService.removeProblemById(id)
        return ApiResponse.success(true)
    }

    @DeleteMapping("/problems")
    fun deleteProblems(
        @RequestBody deleteRequestDto: ProblemDeleteRequestDto
    ): ApiResponse<Boolean> {
        problemService.removeProblemsById(deleteRequestDto.ids)
        return ApiResponse.success(true)
    }

    @PostMapping("/user-answers")
    fun createUserAnswers(
        @RequestBody userAnswers: UserAnswerBatchInsertDto
    ): ApiResponse<UpsertSuccessResponseDto> {
        val size = userAnswerService.createUserAnswers(userAnswers.userAnswers)
        return ApiResponse.success(UpsertSuccessResponseDto(size = size))
    }

    @PostMapping("/user-answer")
    fun createUserAnswer(
        @RequestBody userAnswer: UserAnswerUpsertDto
    ): ApiResponse<UpsertSuccessResponseDto> {
        val createUserAnswerId = userAnswerService.createUserAnswer(userAnswer)
        return ApiResponse.success(UpsertSuccessResponseDto(id = createUserAnswerId))
    }

    @GetMapping("/user-answers/{id}")
    fun getUserAnswerById(
        @PathVariable("id") id: Long
    ): ApiResponse<UserAnswerResponseDto> {
        return ApiResponse.success(userAnswerService.findUserAnswerById(id))
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
                userAnswerService.labelUserAnswer(
                    loginUser.username,
                    id,
                    userAnswerLabelRequestDto.selectedGradingStandardIds
                )

            "validate" ->
                userAnswerService.validateUserAnswer(
                    loginUser.username,
                    id,
                    userAnswerLabelRequestDto.selectedGradingStandardIds
                )

            else -> throw ConditionConflictException(
                ErrorCode.CONDITION_NOT_FULFILLED,
                "존재하지 않는 uri ( $type ) 입니다."
            )
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
        return ApiResponse.success(problemService.findLongProblems(id, title, description, pageable))
    }

    @GetMapping("/problems/short")
    fun findShortProblemsByQuery(
        @RequestParam("id", required = false) id: Long?,
        @RequestParam("title", required = false) title: String?,
        @RequestParam("description", required = false) description: String?,
        pageable: Pageable
    ): ApiResponse<ShortProblemSearchResponseDto> {
        return ApiResponse.success(problemService.findShortProblems(id, title, description, pageable))
    }

    @GetMapping("/problems/multiple")
    fun findMultipleChoiceProblemsByQuery(
        @RequestParam("id", required = false) id: Long?,
        @RequestParam("title", required = false) title: String?,
        @RequestParam("description", required = false) description: String?,
        pageable: Pageable
    ): ApiResponse<MultipleChoiceProblemSearchResponseDto> {
        return ApiResponse.success(problemService.findMultipleProblems(id, title, description, pageable))
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
            userAnswerService.findUserAnswersByQuery(
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

    @DeleteMapping("/user-answers/{id}")
    fun deleteUserAnswerById(
        @PathVariable("id") id: Long
    ): ApiResponse<Boolean> {
        userAnswerService.removeUserAnswerById(id)
        return ApiResponse.success(true)
    }

    @PutMapping("/user-answers/assign/{type:label|validate}")
    fun assignUserAnswer(
        @PathVariable("type") type: String,
        @RequestBody assignUserAnswerDto: AssignUserAnswerDto
    ): ApiResponse<UpsertSuccessResponseDto> {
        if (assignUserAnswerDto.userAnswerIds.isEmpty()) {
            throw ConditionConflictException(ErrorCode.CONDITION_NOT_FULFILLED, "할당 할 user answer가 없습니다.")
        }

        when (type) {
            "label" ->
                userAnswerService.assignLabelUserAnswer(
                    assignUserAnswerDto.userAnswerIds,
                    assignUserAnswerDto.assigneeId
                )

            "validate" ->
                userAnswerService.assignValidationUserAnswer(
                    assignUserAnswerDto.userAnswerIds,
                    assignUserAnswerDto.assigneeId
                )

            else -> throw ConditionConflictException(
                ErrorCode.CONDITION_NOT_FULFILLED,
                "존재하지 않는 uri ( $type ) 입니다."
            )
        }

        return ApiResponse.success(UpsertSuccessResponseDto(size = assignUserAnswerDto.userAnswerIds.size))
    }

    @PostMapping("/notification")
    fun createNotification(
        @RequestBody createNotificationDto: NotificationRequestDto
    ): ApiResponse<UpsertSuccessResponseDto> {
        return ApiResponse.success(
            UpsertSuccessResponseDto(
                id = notificationService.createNotification(createNotificationDto)
            )
        )
    }

    @PostMapping("/notifications")
    fun createBulkNotifications(
        @RequestBody notificationBulkInsertDto: NotificationBulkInsertDto
    ): ApiResponse<UpsertSuccessResponseDto> {
        return ApiResponse.success(
            UpsertSuccessResponseDto(
                size = notificationService.createBulkNotification(notificationBulkInsertDto.content)
            )
        )
    }

    @PostMapping("/problem-sets")
    fun createProblemSet(
        @RequestBody problemSetUpsertRequestDto: ProblemSetUpsertRequestDto
    ): ApiResponse<Long> {
        return ApiResponse.success(problemSetService.createProblemSet(problemSetUpsertRequestDto))
    }

    @PostMapping("/problem-sets/{id}")
    fun updateProblemSet(
        @PathVariable("id") id: Long,
        @RequestBody problemSetUpsertRequestDto: ProblemSetUpsertRequestDto
    ): ApiResponse<Long> {
        return ApiResponse.success(problemSetService.updateProblemSet(id, problemSetUpsertRequestDto))
    }
}
