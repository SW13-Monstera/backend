package io.csbroker.apiserver.service.Problem

import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.dto.problem.ProblemDetailResponseDto
import io.csbroker.apiserver.dto.problem.ProblemGradingHistoryDto
import io.csbroker.apiserver.dto.problem.grade.GradingRequestDto
import io.csbroker.apiserver.dto.problem.grade.MultipleProblemGradingRequestDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemGradingHistoryDto
import io.csbroker.apiserver.model.GradingHistory
import io.csbroker.apiserver.repository.GradingHistoryRepository
import io.csbroker.apiserver.repository.MultipleChoiceProblemRepository
import io.csbroker.apiserver.repository.UserRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
@Qualifier("multipleProblemService")
class MultipleProblemServiceImpl (
    private val multipleChoiceProblemRepository: MultipleChoiceProblemRepository,
    private val userRepository: UserRepository,
    private val gradingHistoryRepository: GradingHistoryRepository,
) : ProblemService2 {
    override fun findProblemById(id: Long, email: String?): ProblemDetailResponseDto {
        return multipleChoiceProblemRepository.findByIdOrNull(id)?.toDetailResponseDto(email)
            ?: throw EntityNotFoundException("${id}번 문제를 찾을 수 없습니다.")
    }

    @Transactional
    override fun gradingProblem(gradingRequestDto: GradingRequestDto): ProblemGradingHistoryDto {
        // get entities
        val (email, problemId, answerIds) = gradingRequestDto as MultipleProblemGradingRequestDto
        val findUser = userRepository.findByEmail(email)
            ?: throw EntityNotFoundException("$email 을 가진 유저는 존재하지 않습니다.")

        val findProblem = multipleChoiceProblemRepository.findByIdOrNull(problemId)
            ?: throw EntityNotFoundException("${problemId}번 문제는 존재하지 않는 서술형 문제입니다.")

        // check score
        val correctAnswer = findProblem.choicesList.filter {
            it.isAnswer
        }.map {
            it.id!!
        }

        val isAnswer = correctAnswer.size == answerIds.size && correctAnswer.containsAll(answerIds)
        val score = if (isAnswer) findProblem.score else 0.0

        // create grading-history
        val gradingHistory = GradingHistory(
            problem = findProblem,
            user = findUser,
            userAnswer = answerIds.joinToString(","),
            score = score,
        )
        gradingHistoryRepository.save(gradingHistory)

        // create dto
        return MultipleChoiceProblemGradingHistoryDto.createDto(
            gradingHistoryId = gradingHistory.gradingHistoryId!!,
            problem = findProblem,
            userAnswerIds = answerIds,
            score = score,
            isAnswer = isAnswer,
        )
    }

}
