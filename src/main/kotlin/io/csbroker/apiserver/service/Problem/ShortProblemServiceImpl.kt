package io.csbroker.apiserver.service.Problem

import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.dto.problem.ProblemDetailResponseDto
import io.csbroker.apiserver.dto.problem.grade.GradingRequestDto
import io.csbroker.apiserver.dto.problem.grade.ShortProblemGradingRequestDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemGradingHistoryDto
import io.csbroker.apiserver.model.GradingHistory
import io.csbroker.apiserver.repository.GradingHistoryRepository
import io.csbroker.apiserver.repository.ShortProblemRepository
import io.csbroker.apiserver.repository.UserRepository
import org.springframework.data.repository.findByIdOrNull

class ShortProblemServiceImpl (
    private val shortProblemRepository: ShortProblemRepository,
    private val userRepository: UserRepository,
    private val gradingHistoryRepository: GradingHistoryRepository,
) : ProblemService2{
    override fun findProblemById(id: Long, email: String): ProblemDetailResponseDto {
        return shortProblemRepository.findByIdOrNull(id)?.toDetailResponseDto(email)
            ?: throw EntityNotFoundException("${id}번 문제를 찾을 수 없습니다.")
    }

    override fun gradingProblem(gradingRequest: GradingRequestDto): ShortProblemGradingHistoryDto {
        // get entities
        val (email, problemId, answer) = gradingRequest as ShortProblemGradingRequestDto
        val findUser = userRepository.findByEmail(email)
            ?: throw EntityNotFoundException("$email 을 가진 유저는 존재하지 않습니다.")

        val findProblem = shortProblemRepository.findByIdOrNull(problemId)
            ?: throw EntityNotFoundException("${problemId}번 문제는 존재하지 않는 서술형 문제입니다.")

        // check score
        val isAnswer = findProblem.answer.lowercase() == answer.lowercase()
        val score = if (isAnswer) findProblem.score else 0.0

        // create grading-history
        val gradingHistory = GradingHistory(
            problem = findProblem,
            user = findUser,
            userAnswer = answer,
            score = score,
        )
        gradingHistoryRepository.save(gradingHistory)

        // create dto
        return ShortProblemGradingHistoryDto.createDto(
            gradingHistoryId = gradingHistory.gradingHistoryId!!,
            problem = findProblem,
            userAnswer = answer,
            score = score,
            isAnswer = isAnswer,
        )
    }
}
