package io.csbroker.apiserver.service.Problem

import io.csbroker.apiserver.common.enums.ErrorCode
import io.csbroker.apiserver.common.exception.ConditionConflictException
import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.common.exception.UnAuthorizedException
import io.csbroker.apiserver.dto.problem.ProblemPageResponseDto
import io.csbroker.apiserver.dto.problem.ProblemSearchDto
import io.csbroker.apiserver.dto.problem.challenge.CreateChallengeDto
import io.csbroker.apiserver.dto.problem.grade.AssessmentRequestDto
import io.csbroker.apiserver.model.Challenge
import io.csbroker.apiserver.repository.*
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class CommonProblemServiceImpl(
    private val problemRepository: ProblemRepository,
    private val gradingHistoryRepository: GradingHistoryRepository,
    private val gradingResultAssessmentRepository: GradingResultAssessmentRepository,
    private val userRepository: UserRepository,
    private val challengeRepository: ChallengeRepository
) : CommonProblemService {


    override fun findProblems(problemSearchDto: ProblemSearchDto, pageable: Pageable): ProblemPageResponseDto {
        return ProblemPageResponseDto(problemRepository.findProblemsByQuery(problemSearchDto, pageable))
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

        return gradingResultAssessment.id!!
    }

    @Transactional
    override fun createChallenge(createChallengeDto: CreateChallengeDto) {
        val (email, problemId, content) = createChallengeDto

        val user = userRepository.findByEmail(email)
            ?: throw EntityNotFoundException("$email 을 가진 유저는 존재하지 않습니다.")

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
}