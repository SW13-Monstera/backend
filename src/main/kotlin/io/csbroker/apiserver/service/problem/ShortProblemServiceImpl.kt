package io.csbroker.apiserver.service.problem

import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.controller.v2.problem.response.ShortProblemDetailResponseV2Dto
import io.csbroker.apiserver.dto.problem.grade.ShortProblemGradingRequestDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemDetailResponseDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemGradingHistoryDto
import io.csbroker.apiserver.model.GradingHistory
import io.csbroker.apiserver.repository.problem.GradingHistoryRepository
import io.csbroker.apiserver.repository.problem.ShortProblemRepository
import io.csbroker.apiserver.repository.user.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ShortProblemServiceImpl(
    private val shortProblemRepository: ShortProblemRepository,
    private val userRepository: UserRepository,
    private val gradingHistoryRepository: GradingHistoryRepository,
) : ShortProblemService {
    override fun findProblemById(id: Long, email: String?): ShortProblemDetailResponseDto {
        return shortProblemRepository.findByIdOrNull(id)?.toDetailResponseDto(email)
            ?: throw EntityNotFoundException("${id}번 문제를 찾을 수 없습니다.")
    }

    @Transactional
    override fun gradingProblem(gradingRequest: ShortProblemGradingRequestDto): ShortProblemGradingHistoryDto {
        // get entities
        val (user, problemId, answer) = gradingRequest

        val findProblem = shortProblemRepository.findByIdOrNull(problemId)
            ?: throw EntityNotFoundException("${problemId}번 문제는 존재하지 않는 서술형 문제입니다.")

        // check score
        val isAnswer = findProblem.answer.lowercase() == answer.lowercase()
        val score = if (isAnswer) findProblem.score else 0.0

        // create grading-history
        val gradingHistory = gradingHistoryRepository.save(
            GradingHistory(
                problem = findProblem,
                user = user,
                userAnswer = answer,
                score = score,
            ),
        )

        // create dto
        return ShortProblemGradingHistoryDto.createDto(
            gradingHistoryId = gradingHistory.gradingHistoryId,
            problem = findProblem,
            userAnswer = answer,
            score = score,
            isAnswer = isAnswer,
        )
    }

    override fun findShortProblemDetailByIdV2(id: Long, email: String?): ShortProblemDetailResponseV2Dto {
        return shortProblemRepository.findByIdOrNull(id)?.toDetailResponseV2Dto(email)
            ?: throw EntityNotFoundException("${id}번 문제를 찾을 수 없습니다.")
    }
}
