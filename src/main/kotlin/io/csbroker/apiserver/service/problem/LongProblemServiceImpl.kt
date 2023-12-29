package io.csbroker.apiserver.service.problem

import io.csbroker.apiserver.common.enums.LikeType
import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.controller.v2.problem.request.SubmitLongProblemDto
import io.csbroker.apiserver.controller.v2.problem.response.SubmitLongProblemResponseDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemDetailResponseDto
import io.csbroker.apiserver.model.GradingHistory
import io.csbroker.apiserver.model.UserAnswer
import io.csbroker.apiserver.repository.post.LikeRepository
import io.csbroker.apiserver.repository.problem.GradingHistoryRepository
import io.csbroker.apiserver.repository.problem.LongProblemRepository
import io.csbroker.apiserver.repository.problem.StandardAnswerRepository
import io.csbroker.apiserver.repository.problem.UserAnswerRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class LongProblemServiceImpl(
    private val longProblemRepository: LongProblemRepository,
    private val userAnswerRepository: UserAnswerRepository,
    private val standardAnswerRepository: StandardAnswerRepository,
    private val gradingHistoryRepository: GradingHistoryRepository,
    private val likeRepository: LikeRepository,
) : LongProblemService {
    override fun findProblemById(id: Long, email: String?): LongProblemDetailResponseDto {
        val likes = likeRepository.findAllByTargetId(LikeType.PROBLEM, id)
        return longProblemRepository.findByIdOrNull(id)?.toDetailResponseDto(email, likes)
            ?: throw EntityNotFoundException("${id}번 문제를 찾을 수 없습니다.")
    }

    @Transactional
    override fun submitProblem(submitRequest: SubmitLongProblemDto): SubmitLongProblemResponseDto {
        val (user, problemId, answer) = submitRequest

        val problem = longProblemRepository.findByIdOrNull(problemId)
            ?: throw EntityNotFoundException("${problemId}번 문제는 존재하지 않는 서술형 문제입니다.")

        val userAnswer = UserAnswer(answer = answer, problem = problem)
        userAnswerRepository.save(userAnswer)

        val standardAnswer = standardAnswerRepository.findAllByLongProblem(problem).takeIf {
            it.isNotEmpty()
        }?.random()?.content ?: throw EntityNotFoundException("{$problem.id}번 문제에 대한 모범답안이 존재하지 않습니다.")

        val tags = problem.problemTags.map {
            it.tag.name
        }

        val gradingHistory = GradingHistory(
            problem = problem,
            user = user,
            userAnswer = answer,
            score = 0.0,
        )
        gradingHistoryRepository.save(gradingHistory)

        val gradingHistories = problem.gradingHistory
        val totalSubmissionCount = gradingHistories.size
        val userSubmissionCount = gradingHistories.count { it.user.id == user.id }

        return SubmitLongProblemResponseDto(
            title = problem.title,
            tags = tags,
            description = problem.description,
            totalSubmissionCount = totalSubmissionCount,
            userSubmissionCount = userSubmissionCount,
            userAnswer = answer,
            standardAnswer = standardAnswer,
        )
    }

}
