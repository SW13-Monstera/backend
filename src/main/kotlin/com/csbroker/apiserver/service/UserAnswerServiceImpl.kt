package com.csbroker.apiserver.service

import com.csbroker.apiserver.common.enums.ErrorCode
import com.csbroker.apiserver.common.exception.ConditionConflictException
import com.csbroker.apiserver.common.exception.EntityNotFoundException
import com.csbroker.apiserver.dto.UserAnswerResponseDto
import com.csbroker.apiserver.dto.UserAnswerSearchResponseDto
import com.csbroker.apiserver.dto.UserAnswerUpsertDto
import com.csbroker.apiserver.model.UserAnswer
import com.csbroker.apiserver.repository.GradingStandardRepository
import com.csbroker.apiserver.repository.LongProblemRepository
import com.csbroker.apiserver.repository.UserAnswerGradingStandardRepository
import com.csbroker.apiserver.repository.UserAnswerRepository
import com.csbroker.apiserver.repository.UserRepository
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserAnswerServiceImpl(
    private val userAnswerRepository: UserAnswerRepository,
    private val userRepository: UserRepository,
    private val longProblemRepository: LongProblemRepository,
    private val gradingStandardRepository: GradingStandardRepository,
    private val userAnswerGradingStandardRepository: UserAnswerGradingStandardRepository
) : UserAnswerService {
    @Transactional
    override fun createUserAnswers(userAnswers: List<UserAnswerUpsertDto>): Int {
        this.userAnswerRepository.batchInsert(userAnswers)
        return userAnswers.size
    }

    @Transactional
    override fun createUserAnswer(userAnswer: UserAnswerUpsertDto): Long {
        val problem = longProblemRepository.findByIdOrNull(userAnswer.problemId)
            ?: throw EntityNotFoundException("${userAnswer.problemId}번 문제를 찾을 수 없습니다.")

        val assignedUser =
            if (userAnswer.assignedUserId == null) null
            else userRepository.findByIdOrNull(userAnswer.assignedUserId)
                ?: throw EntityNotFoundException(
                    "${userAnswer.assignedUserId}의 아이디를 가진 유저를 찾을 수 없습니다."
                )

        val validatingUser =
            if (userAnswer.validatingUserId == null) null
            else userRepository.findByIdOrNull(userAnswer.validatingUserId)
                ?: throw EntityNotFoundException(
                    "${userAnswer.validatingUserId}의 아이디를 가진 유저를 찾을 수 없습니다."
                )

        val createUserAnswer = UserAnswer(
            answer = userAnswer.answer,
            problem = problem,
            assignedUser = assignedUser,
            validatingUser = validatingUser
        )

        return this.userAnswerRepository.save(createUserAnswer).id!!
    }

    override fun findUserAnswerById(id: Long): UserAnswerResponseDto {
        val userAnswer = this.userAnswerRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("${id}번 유저 응답을 찾을 수 없습니다.")

        return UserAnswerResponseDto.fromUserAnswer(userAnswer)
    }

    @Transactional
    override fun labelUserAnswer(email: String, userAnswerId: Long, selectedGradingStandardIds: List<Long>): Long {
        val userAnswer = this.userAnswerRepository.findByIdOrNull(userAnswerId)
            ?: throw EntityNotFoundException(
                "${userAnswerId}번 유저 응답을 찾을 수 없습니다."
            )

        if (userAnswer.assignedUser == null || userAnswer.assignedUser!!.email != email) {
            throw EntityNotFoundException("${userAnswerId}번에 할당된 유저가 아닙니다.")
        }

        this.setGradingStandards(selectedGradingStandardIds, userAnswerId)

        userAnswer.isLabeled = true

        return userAnswerId
    }

    @Transactional
    override fun validateUserAnswer(email: String, userAnswerId: Long, selectedGradingStandardIds: List<Long>): Long {
        val userAnswer = this.userAnswerRepository.findByIdOrNull(userAnswerId)
            ?: throw EntityNotFoundException(
                "${userAnswerId}번 유저 응답을 찾을 수 없습니다."
            )

        if (!userAnswer.isLabeled) {
            throw ConditionConflictException(
                ErrorCode.CONDITION_NOT_FULFILLED,
                "${userAnswerId}번 유저 응답은 라벨링 되지 않았기때문에, 검수할 수 없습니다."
            )
        }

        if (userAnswer.validatingUser == null || userAnswer.validatingUser!!.email != email) {
            throw ConditionConflictException(
                ErrorCode.CONDITION_NOT_FULFILLED,
                "${userAnswerId}번에 검수자로 할당된 유저가 아닙니다."
            )
        }

        this.setGradingStandards(selectedGradingStandardIds, userAnswerId)

        userAnswer.isValidated = true

        return userAnswerId
    }

    private fun setGradingStandards(
        selectedGradingStandardIds: List<Long>,
        userAnswerId: Long
    ) {
        val foundGradingStandardsCount = this.gradingStandardRepository
            .countByIdIn(selectedGradingStandardIds)

        if (foundGradingStandardsCount != selectedGradingStandardIds.size) {
            throw EntityNotFoundException("존재하지 않는 채점 기준으로 라벨링을 시도하였습니다.")
        }

        this.userAnswerGradingStandardRepository.deleteAllByUserAnswerId(userAnswerId)

        this.userAnswerGradingStandardRepository.batchInsert(userAnswerId, selectedGradingStandardIds)
    }

    override fun findUserAnswersByQuery(
        id: Long?,
        assignedBy: String?,
        validatedBy: String?,
        problemTitle: String?,
        answer: String?,
        isLabeled: Boolean?,
        isValidated: Boolean?,
        pageable: Pageable
    ): UserAnswerSearchResponseDto {
        val pagedUserAnswers = this.userAnswerRepository.findUserAnswersByQuery(
            id,
            assignedBy,
            validatedBy,
            problemTitle,
            answer,
            isLabeled,
            isValidated,
            pageable
        )

        return UserAnswerSearchResponseDto(
            pagedUserAnswers.map { it.toUserAnswerDataDto() }.toList(),
            pagedUserAnswers.totalPages,
            pagedUserAnswers.totalElements
        )
    }
}
