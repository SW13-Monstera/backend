package io.csbroker.apiserver.service

import io.csbroker.apiserver.common.enums.ErrorCode
import io.csbroker.apiserver.common.enums.Role
import io.csbroker.apiserver.common.exception.ConditionConflictException
import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.common.exception.UnAuthorizedException
import io.csbroker.apiserver.dto.useranswer.UserAnswerResponseDto
import io.csbroker.apiserver.dto.useranswer.UserAnswerSearchResponseDto
import io.csbroker.apiserver.dto.useranswer.UserAnswerUpsertDto
import io.csbroker.apiserver.model.UserAnswer
import io.csbroker.apiserver.repository.GradingStandardRepository
import io.csbroker.apiserver.repository.LongProblemRepository
import io.csbroker.apiserver.repository.UserAnswerGradingStandardRepository
import io.csbroker.apiserver.repository.UserAnswerRepository
import io.csbroker.apiserver.repository.UserRepository
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserAnswerServiceImpl(
    private val userAnswerRepository: UserAnswerRepository,
    private val userRepository: UserRepository,
    private val longProblemRepository: LongProblemRepository,
    private val gradingStandardRepository: GradingStandardRepository,
    private val userAnswerGradingStandardRepository: UserAnswerGradingStandardRepository,
) : UserAnswerService {
    @Transactional
    override fun createUserAnswers(userAnswers: List<UserAnswerUpsertDto>): Int {
        userAnswerRepository.batchInsert(userAnswers)
        return userAnswers.size
    }

    @Transactional
    override fun createUserAnswer(userAnswer: UserAnswerUpsertDto): Long {
        val problem = longProblemRepository.findByIdOrNull(userAnswer.problemId)
            ?: throw EntityNotFoundException("${userAnswer.problemId}번 문제를 찾을 수 없습니다.")

        val assignedUser =
            if (userAnswer.assignedUserId == null) {
                null
            } else {
                userRepository.findByIdOrNull(userAnswer.assignedUserId)
                    ?: throw EntityNotFoundException(
                        "${userAnswer.assignedUserId}의 아이디를 가진 유저를 찾을 수 없습니다.",
                    )
            }

        val validatingUser =
            if (userAnswer.validatingUserId == null) {
                null
            } else {
                userRepository.findByIdOrNull(userAnswer.validatingUserId)
                    ?: throw EntityNotFoundException(
                        "${userAnswer.validatingUserId}의 아이디를 가진 유저를 찾을 수 없습니다.",
                    )
            }

        val createUserAnswer = UserAnswer(
            answer = userAnswer.answer,
            problem = problem,
            assignedUser = assignedUser,
            validatingUser = validatingUser,
        )

        return userAnswerRepository.save(createUserAnswer).id!!
    }

    override fun findUserAnswerById(id: Long): UserAnswerResponseDto {
        val userAnswer = userAnswerRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("${id}번 유저 응답을 찾을 수 없습니다.")

        return UserAnswerResponseDto.fromUserAnswer(userAnswer)
    }

    @Transactional
    override fun labelUserAnswer(email: String, userAnswerId: Long, selectedGradingStandardIds: List<Long>): Long {
        val userAnswer = userAnswerRepository.findByIdOrNull(userAnswerId)
            ?: throw EntityNotFoundException(
                "${userAnswerId}번 유저 응답을 찾을 수 없습니다.",
            )

        if (userAnswer.assignedUser == null || userAnswer.assignedUser!!.email != email) {
            throw EntityNotFoundException("${userAnswerId}번에 할당된 유저가 아닙니다.")
        }

        setGradingStandards(selectedGradingStandardIds, userAnswerId)

        userAnswer.isLabeled = true

        return userAnswerId
    }

    @Transactional
    override fun validateUserAnswer(email: String, userAnswerId: Long, selectedGradingStandardIds: List<Long>): Long {
        val userAnswer = userAnswerRepository.findByIdOrNull(userAnswerId)
            ?: throw EntityNotFoundException(
                "${userAnswerId}번 유저 응답을 찾을 수 없습니다.",
            )

        if (!userAnswer.isLabeled) {
            throw ConditionConflictException(
                ErrorCode.CONDITION_NOT_FULFILLED,
                "${userAnswerId}번 유저 응답은 라벨링 되지 않았기때문에, 검수할 수 없습니다.",
            )
        }

        if (userAnswer.validatingUser == null || userAnswer.validatingUser?.email != email) {
            throw ConditionConflictException(
                ErrorCode.CONDITION_NOT_FULFILLED,
                "${userAnswerId}번에 검수자로 할당된 유저가 아닙니다.",
            )
        }

        setGradingStandards(selectedGradingStandardIds, userAnswerId)

        userAnswer.isValidated = true

        return userAnswerId
    }

    private fun setGradingStandards(
        selectedGradingStandardIds: List<Long>,
        userAnswerId: Long,
    ) {
        val foundGradingStandardsCount = gradingStandardRepository
            .countByIdIn(selectedGradingStandardIds)

        if (foundGradingStandardsCount != selectedGradingStandardIds.size) {
            throw EntityNotFoundException("존재하지 않는 채점 기준으로 라벨링을 시도하였습니다.")
        }

        userAnswerGradingStandardRepository.deleteAllByUserAnswerId(userAnswerId)

        userAnswerGradingStandardRepository.batchInsert(userAnswerId, selectedGradingStandardIds)
    }

    override fun findUserAnswersByQuery(
        id: Long?,
        assignedBy: String?,
        validatedBy: String?,
        problemTitle: String?,
        answer: String?,
        isLabeled: Boolean?,
        isValidated: Boolean?,
        pageable: Pageable,
    ): UserAnswerSearchResponseDto {
        val pagedUserAnswers = userAnswerRepository.findUserAnswersByQuery(
            id,
            assignedBy,
            validatedBy,
            problemTitle,
            answer,
            isLabeled,
            isValidated,
            pageable,
        )

        return UserAnswerSearchResponseDto(
            pagedUserAnswers.map { it.toUserAnswerDataDto() }.toList(),
            pagedUserAnswers.totalPages,
            pagedUserAnswers.totalElements,
        )
    }

    @Transactional
    override fun assignLabelUserAnswer(userAnswerIds: List<Long>, userId: UUID) {
        validateAssignCondition(userAnswerIds, userId)
        userAnswerRepository.updateLabelerId(userAnswerIds, userId)
    }

    @Transactional
    override fun assignValidationUserAnswer(userAnswerIds: List<Long>, userId: UUID) {
        validateAssignCondition(userAnswerIds, userId)
        userAnswerRepository.updateValidatorId(userAnswerIds, userId)
    }

    @Transactional
    override fun removeUserAnswerById(userAnswerId: Long) {
        userAnswerRepository.deleteById(userAnswerId)
    }

    private fun validateAssignCondition(userAnswerIds: List<Long>, userId: UUID) {
        val cnt = userAnswerRepository.cntUserAnswer(userAnswerIds)
        if (cnt != userAnswerIds.size) {
            throw EntityNotFoundException("존재하지 않는 user answer를 업데이트 할 수 없습니다.")
        }

        val findUser = userRepository.findByIdOrNull(userId)
            ?: throw EntityNotFoundException("${userId}를 가진 유저를 찾을 수 없습니다.")

        if (findUser.role != Role.ROLE_ADMIN) {
            throw UnAuthorizedException(ErrorCode.FORBIDDEN, "권한이 없는 유저를 할당하려 하였습니다.")
        }
    }
}
