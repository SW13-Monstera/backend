package io.csbroker.apiserver.service.problem

import io.csbroker.apiserver.common.enums.ErrorCode
import io.csbroker.apiserver.common.enums.LikeType
import io.csbroker.apiserver.common.exception.ConditionConflictException
import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.common.exception.UnAuthorizedException
import io.csbroker.apiserver.dto.problem.ProblemPageResponseDto
import io.csbroker.apiserver.dto.problem.ProblemSearchDto
import io.csbroker.apiserver.dto.problem.ProblemsResponseDto
import io.csbroker.apiserver.dto.problem.challenge.CreateChallengeDto
import io.csbroker.apiserver.dto.problem.grade.AssessmentRequestDto
import io.csbroker.apiserver.model.Challenge
import io.csbroker.apiserver.model.Like
import io.csbroker.apiserver.model.ProblemBookmark
import io.csbroker.apiserver.model.User
import io.csbroker.apiserver.repository.post.LikeRepository
import io.csbroker.apiserver.repository.problem.ChallengeRepository
import io.csbroker.apiserver.repository.problem.GradingHistoryRepository
import io.csbroker.apiserver.repository.problem.GradingResultAssessmentRepository
import io.csbroker.apiserver.repository.problem.ProblemBookmarkRepository
import io.csbroker.apiserver.repository.problem.ProblemRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class CommonProblemServiceImpl(
    private val problemRepository: ProblemRepository,
    private val gradingHistoryRepository: GradingHistoryRepository,
    private val gradingResultAssessmentRepository: GradingResultAssessmentRepository,
    private val challengeRepository: ChallengeRepository,
    private val likeRepository: LikeRepository,
    private val problemBookmarkRepository: ProblemBookmarkRepository,
) : CommonProblemService {
    override fun findProblems(problemSearchDto: ProblemSearchDto): ProblemPageResponseDto {
        return ProblemPageResponseDto(problemRepository.findProblemsByQuery(problemSearchDto))
    }

    override fun findRandomProblems(size: Int): ProblemsResponseDto {
        val problemIds = problemRepository.findRandomProblemIds(size)
        val problems = problemRepository.findAllById(problemIds)
        val problemIdToStatMap = problemRepository.getProblemIdToStatMap(problems)
        return ProblemsResponseDto(
            problems.map {
                it.toProblemResponseDto(problemIdToStatMap[it.id])
            },
        )
    }

    @Transactional
    override fun removeProblemById(id: Long) {
        problemRepository.deleteById(id)
    }

    @Transactional
    override fun removeProblemsById(ids: List<Long>) {
        problemRepository.deleteProblemsByIdIn(ids)
    }

    @Transactional
    override fun gradingAssessment(
        email: String,
        gradingHistoryId: Long,
        assessmentRequestDto: AssessmentRequestDto,
    ): Long {
        val gradingHistory = gradingHistoryRepository.findByIdOrNull(gradingHistoryId)
            ?: throw EntityNotFoundException("$gradingHistoryId 번의 채점 기록은 찾을 수 없습니다.")

        if (gradingHistory.gradingResultAssessment != null) {
            throw ConditionConflictException(
                ErrorCode.CONDITION_NOT_FULFILLED,
                "$gradingHistoryId 번 채점 기록에 대한 평가가 이미 존재합니다!",
            )
        }

        if (gradingHistory.user.email != email) {
            throw UnAuthorizedException(
                ErrorCode.FORBIDDEN,
                "$email 유저는 $gradingHistoryId 번 채점 기록을 제출한 유저가 아닙니다.",
            )
        }

        val gradingResultAssessment =
            gradingResultAssessmentRepository.save(assessmentRequestDto.toGradingResultAssessment(gradingHistory))

        gradingHistory.gradingResultAssessment = gradingResultAssessment

        return gradingResultAssessment.id
    }

    @Transactional
    override fun createChallenge(createChallengeDto: CreateChallengeDto) {
        val (user, problemId, content) = createChallengeDto

        val problem = problemRepository.findByIdOrNull(problemId)
            ?: throw EntityNotFoundException("${problemId}번 문제는 존재하지 않는 문제입니다.")

        challengeRepository.save(
            Challenge(
                user = user,
                content = content,
                problem = problem,
            ),
        )
    }

    @Transactional
    override fun likeProblem(user: User, problemId: Long) {
        problemRepository.findByIdOrNull(problemId)
            ?: throw EntityNotFoundException("${problemId}번 문제는 존재하지 않는 문제입니다.")
        likeRepository.findByTargetIdAndUser(LikeType.PROBLEM, problemId, user)
            ?.let { likeRepository.delete(it) }
            ?: likeRepository.save(Like(user = user, type = LikeType.PROBLEM, targetId = problemId))
    }

    @Transactional
    override fun bookmarkProblem(user: User, problemId: Long) {
        val problem = problemRepository.findByIdOrNull(problemId)
            ?: throw EntityNotFoundException("${problemId}번 문제는 존재하지 않는 문제입니다.")
        val problemBookmark = problemBookmarkRepository.findByUserAndProblem(user, problem)
        if (problemBookmark == null) {
            problemBookmarkRepository.save(ProblemBookmark(user = user, problem = problem))
        } else {
            problemBookmarkRepository.delete(problemBookmark)
        }
    }
}
