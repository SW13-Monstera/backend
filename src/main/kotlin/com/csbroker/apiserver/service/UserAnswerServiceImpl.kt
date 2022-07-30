package com.csbroker.apiserver.service

import com.csbroker.apiserver.dto.UserAnswerLabelRequestDto
import com.csbroker.apiserver.dto.UserAnswerResponseDto
import com.csbroker.apiserver.dto.UserAnswerUpsertDto
import com.csbroker.apiserver.model.UserAnswer
import com.csbroker.apiserver.repository.GradingStandardRepository
import com.csbroker.apiserver.repository.LongProblemRepository
import com.csbroker.apiserver.repository.UserAnswerGradingStandardRepository
import com.csbroker.apiserver.repository.UserAnswerRepository
import com.csbroker.apiserver.repository.UserRepository
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
            ?: throw IllegalArgumentException("${userAnswer.problemId}번 문제를 찾을 수 없습니다.")

        val assignedUser =
            if (userAnswer.assignedUserId == null) null
            else userRepository.findByIdOrNull(userAnswer.assignedUserId)
                ?: throw IllegalArgumentException(
                    "${userAnswer.assignedUserId}의 아이디를 가진 유저를 찾을 수 없습니다."
                )

        val validatingUser =
            if (userAnswer.validatingUserId == null) null
            else userRepository.findByIdOrNull(userAnswer.validatingUserId)
                ?: throw IllegalArgumentException(
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
        val userAnswer = (this.userAnswerRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("${id}번 유저 응답을 찾을 수 없습니다."))

        return UserAnswerResponseDto.fromUserAnswer(userAnswer)
    }

    @Transactional
    override fun labelUserAnswer(userAnswerId: Long, selectedGradingStandardIds: List<Long>): Long {
        val userAnswer = this.userAnswerRepository.findByIdOrNull(userAnswerId)
            ?: throw IllegalArgumentException(
                "${userAnswerId}번 유저 응답을 찾을 수 없습니다."
            )

        val foundGradingStandardsCount = this.gradingStandardRepository
            .countByIdIn(selectedGradingStandardIds)

        if (foundGradingStandardsCount != selectedGradingStandardIds.size) {
            throw IllegalArgumentException("존재하지 않는 채점 기준으로 라벨링을 시도하였습니다.")
        }

        this.userAnswerGradingStandardRepository.deleteAllByUserAnswerId(userAnswerId)

        this.userAnswerGradingStandardRepository.batchInsert(userAnswerId, selectedGradingStandardIds)

        userAnswer.isLabeled = true

        return userAnswerId
    }
}
