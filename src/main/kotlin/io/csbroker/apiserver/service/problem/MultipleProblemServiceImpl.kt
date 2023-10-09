package io.csbroker.apiserver.service.problem

import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.dto.problem.grade.MultipleProblemGradingRequestDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemDetailResponseDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemGradingHistoryDto
import io.csbroker.apiserver.model.GradingHistory
import io.csbroker.apiserver.repository.problem.GradingHistoryRepository
import io.csbroker.apiserver.repository.problem.MultipleChoiceProblemRepository
import io.csbroker.apiserver.repository.user.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class MultipleProblemServiceImpl(
    private val multipleChoiceProblemRepository: MultipleChoiceProblemRepository,
    private val userRepository: UserRepository,
    private val gradingHistoryRepository: GradingHistoryRepository,
) : MultipleProblemService {
    override fun findProblemById(id: Long, email: String?): MultipleChoiceProblemDetailResponseDto {
        return multipleChoiceProblemRepository.findByIdOrNull(id)?.toDetailResponseDto(email)
            ?: throw EntityNotFoundException("${id}번 문제를 찾을 수 없습니다.")
    }

    @Transactional
    override fun gradingProblem(
        gradingRequest: MultipleProblemGradingRequestDto,
    ): MultipleChoiceProblemGradingHistoryDto {
        // get entities
        val (user, problemId, answerIds) = gradingRequest

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
        val gradingHistory = gradingHistoryRepository.save(
            GradingHistory(
                problem = findProblem,
                user = user,
                userAnswer = answerIds.joinToString(","),
                score = score,
            ),
        )

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
