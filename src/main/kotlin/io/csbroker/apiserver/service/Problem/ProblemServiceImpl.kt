package io.csbroker.apiserver.service.Problem

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.csbroker.apiserver.common.client.AIServerClient
import io.csbroker.apiserver.common.enums.ErrorCode
import io.csbroker.apiserver.common.enums.GradingStandardType
import io.csbroker.apiserver.common.exception.ConditionConflictException
import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.common.exception.InternalServiceException
import io.csbroker.apiserver.common.exception.UnAuthorizedException
import io.csbroker.apiserver.common.util.log
import io.csbroker.apiserver.controller.v2.response.ShortProblemDetailResponseV2Dto
import io.csbroker.apiserver.dto.problem.ProblemPageResponseDto
import io.csbroker.apiserver.dto.problem.ProblemSearchDto
import io.csbroker.apiserver.dto.problem.challenge.CreateChallengeDto
import io.csbroker.apiserver.dto.problem.grade.AssessmentRequestDto
import io.csbroker.apiserver.dto.problem.grade.GradeResultDto
import io.csbroker.apiserver.dto.problem.grade.LongProblemGradingRequestToAiServerDto
import io.csbroker.apiserver.dto.problem.grade.KeywordGradingRequestDto
import io.csbroker.apiserver.dto.problem.longproblem.ContentDto
import io.csbroker.apiserver.dto.problem.longproblem.KeywordDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemDetailResponseDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemGradingHistoryDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemResponseDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemSearchResponseDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemUpsertRequestDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemDetailResponseDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemGradingHistoryDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemResponseDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemSearchResponseDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemUpsertRequestDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemDetailResponseDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemGradingHistoryDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemResponseDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemSearchResponseDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemUpsertRequestDto
import io.csbroker.apiserver.model.Challenge
import io.csbroker.apiserver.model.GradingHistory
import io.csbroker.apiserver.model.LongProblem
import io.csbroker.apiserver.model.Problem
import io.csbroker.apiserver.model.ProblemTag
import io.csbroker.apiserver.model.UserAnswer
import io.csbroker.apiserver.repository.ChallengeRepository
import io.csbroker.apiserver.repository.ChoiceRepository
import io.csbroker.apiserver.repository.GradingHistoryRepository
import io.csbroker.apiserver.repository.GradingResultAssessmentRepository
import io.csbroker.apiserver.repository.GradingStandardRepository
import io.csbroker.apiserver.repository.LongProblemRepository
import io.csbroker.apiserver.repository.MultipleChoiceProblemRepository
import io.csbroker.apiserver.repository.ProblemRepository
import io.csbroker.apiserver.repository.ProblemTagRepository
import io.csbroker.apiserver.repository.ShortProblemRepository
import io.csbroker.apiserver.repository.StandardAnswerRepository
import io.csbroker.apiserver.repository.TagRepository
import io.csbroker.apiserver.repository.UserAnswerRepository
import io.csbroker.apiserver.repository.UserRepository
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ProblemServiceImpl(
    private val problemRepository: ProblemRepository,
    private val shortProblemRepository: ShortProblemRepository,
    private val longProblemRepository: LongProblemRepository,
    private val multipleChoiceProblemRepository: MultipleChoiceProblemRepository,
    private val problemTagRepository: ProblemTagRepository,
    private val userRepository: UserRepository,
    private val choiceRepository: ChoiceRepository,
    private val tagRepository: TagRepository,
    private val gradingStandardRepository: GradingStandardRepository,
    private val gradingHistoryRepository: GradingHistoryRepository,
    private val userAnswerRepository: UserAnswerRepository,
    private val aiServerClient: AIServerClient,
    private val gradingResultAssessmentRepository: GradingResultAssessmentRepository,
    private val challengeRepository: ChallengeRepository,
    private val standardAnswerRepository: StandardAnswerRepository,
) : ProblemService {

    override fun findProblems(problemSearchDto: ProblemSearchDto, pageable: Pageable): ProblemPageResponseDto {
        return ProblemPageResponseDto(problemRepository.findProblemsByQuery(problemSearchDto, pageable))
    }

    override fun findLongProblems(
        id: Long?,
        title: String?,
        description: String?,
        pageable: Pageable,
    ): LongProblemSearchResponseDto {
        val pagedProblems = longProblemRepository
            .findLongProblemsByQuery(id, title, description, pageable)

        return LongProblemSearchResponseDto(
            pagedProblems.map { it.toLongProblemDataDto() }.toList(),
            pagedProblems.totalPages,
            pagedProblems.totalElements,
        )
    }

    override fun findShortProblems(
        id: Long?,
        title: String?,
        description: String?,
        pageable: Pageable,
    ): ShortProblemSearchResponseDto {
        val pagedProblems = shortProblemRepository
            .findShortProblemsByQuery(id, title, description, pageable)

        return ShortProblemSearchResponseDto(
            pagedProblems.map { it.toShortProblemDataDto() }.toList(),
            pagedProblems.totalPages,
            pagedProblems.totalElements,
        )
    }

    override fun findMultipleProblems(
        id: Long?,
        title: String?,
        description: String?,
        pageable: Pageable,
    ): MultipleChoiceProblemSearchResponseDto {
        val pagedProblems = multipleChoiceProblemRepository
            .findMultipleChoiceProblemsByQuery(id, title, description, pageable)

        return MultipleChoiceProblemSearchResponseDto(
            pagedProblems.map { it.toMultipleChoiceDataDto() }.toList(),
            pagedProblems.totalPages,
            pagedProblems.totalElements,
        )
    }

    override fun findLongProblemDetailById(id: Long, email: String?): LongProblemDetailResponseDto {
        return longProblemRepository.findByIdOrNull(id)?.toDetailResponseDto(email)
            ?: throw EntityNotFoundException("${id}번 문제를 찾을 수 없습니다.")
    }

    override fun findShortProblemDetailById(id: Long, email: String?): ShortProblemDetailResponseDto {
        return shortProblemRepository.findByIdOrNull(id)?.toDetailResponseDto(email)
            ?: throw EntityNotFoundException("${id}번 문제를 찾을 수 없습니다.")
    }

    override fun findShortProblemDetailByIdV2(id: Long, email: String?): ShortProblemDetailResponseV2Dto {
        return shortProblemRepository.findByIdOrNull(id)?.toDetailResponseV2Dto(email)
            ?: throw EntityNotFoundException("${id}번 문제를 찾을 수 없습니다.")
    }

    override fun findMultipleChoiceProblemDetailById(id: Long, email: String?): MultipleChoiceProblemDetailResponseDto {
        return multipleChoiceProblemRepository.findByIdOrNull(id)?.toDetailResponseDto(email)
            ?: throw EntityNotFoundException("${id}번 문제를 찾을 수 없습니다.")
    }

    override fun findLongProblemById(id: Long): LongProblemResponseDto {
        val longProblem = longProblemRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("${id}번 문제는 존재하지 않는 서술형 문제입니다.")
        return longProblem.toLongProblemResponseDto()
    }

    override fun findShortProblemById(id: Long): ShortProblemResponseDto {
        val shortProblem = shortProblemRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("${id}번 문제는 존재하지 않는 단답형 문제입니다.")
        return shortProblem.toShortProblemResponseDto()
    }

    override fun findMultipleProblemById(id: Long): MultipleChoiceProblemResponseDto {
        val multipleChoiceProblem = multipleChoiceProblemRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("${id}번 문제는 존재하지 않는 객관식 문제입니다.")
        return multipleChoiceProblem.toMultipleChoiceProblemResponseDto()
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
    override fun createLongProblem(createRequestDto: LongProblemUpsertRequestDto, email: String): Long {
        val findUser = userRepository.findByEmail(email)
            ?: throw EntityNotFoundException("$email 을 가진 유저는 존재하지 않습니다.")
        val longProblem = createRequestDto.toLongProblem(findUser)
        val gradingStandardList = createRequestDto.getGradingStandardList(longProblem)

        longProblem.addGradingStandards(gradingStandardList)
        setTags(longProblem, createRequestDto.tags)

        return problemRepository.save(longProblem).id!!
    }

    @Transactional
    override fun createShortProblem(createRequestDto: ShortProblemUpsertRequestDto, email: String): Long {
        val findUser = userRepository.findByEmail(email)
            ?: throw EntityNotFoundException("$email 을 가진 유저는 존재하지 않습니다.")
        val shortProblem = createRequestDto.toShortProblem(findUser)

        setTags(shortProblem, createRequestDto.tags)

        return problemRepository.save(shortProblem).id!!
    }

    @Transactional
    override fun createMultipleChoiceProblem(
        createRequestDto: MultipleChoiceProblemUpsertRequestDto,
        email: String,
    ): Long {
        val findUser = userRepository.findByEmail(email)
            ?: throw EntityNotFoundException("$email 을 가진 유저는 존재하지 않습니다.")

        val multipleChoiceProblem = createRequestDto.toMultipleChoiceProblem(findUser)
        val choiceDataList = createRequestDto.getChoiceList(multipleChoiceProblem)

        setTags(multipleChoiceProblem, createRequestDto.tags)

        if (choiceDataList.count { it.isAnswer } == 0) {
            throw ConditionConflictException(ErrorCode.CONDITION_NOT_FULFILLED, "답의 개수는 1개 이상이여야합니다.")
        }

        multipleChoiceProblem.addChoices(choiceDataList)

        return problemRepository.save(multipleChoiceProblem).id!!
    }

    @Transactional
    override fun updateLongProblem(id: Long, updateRequestDto: LongProblemUpsertRequestDto, email: String): Long {
        val findProblem = longProblemRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("${id}번 문제는 존재하지 않는 서술형 문제입니다.")

        gradingStandardRepository.deleteAllById(findProblem.gradingStandards.map { it.id })

        findProblem.gradingStandards.clear()

        val gradingStandardList = updateRequestDto.getGradingStandardList(findProblem)

        findProblem.addGradingStandards(gradingStandardList)

        findProblem.updateFromDto(updateRequestDto)

        updateTags(findProblem, updateRequestDto.tags)

        return id
    }

    @Transactional
    override fun updateShortProblem(id: Long, updateRequestDto: ShortProblemUpsertRequestDto, email: String): Long {
        val findProblem = shortProblemRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("${id}번 문제는 존재하지 않는 단답형 문제입니다.")

        updateTags(findProblem, updateRequestDto.tags)
        findProblem.updateFromDto(updateRequestDto)

        return id
    }

    @Transactional
    override fun updateMultipleChoiceProblem(
        id: Long,
        updateRequestDto: MultipleChoiceProblemUpsertRequestDto,
        email: String,
    ): Long {
        val findProblem = multipleChoiceProblemRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("${id}번 문제는 존재하지 않는 객관식 문제입니다.")

        val choiceDataList = updateRequestDto.getChoiceList(findProblem)

        if (choiceDataList.count { it.isAnswer } == 0) {
            throw ConditionConflictException(ErrorCode.CONDITION_NOT_FULFILLED, "답의 개수는 1개 이상이여야합니다.")
        }

        choiceRepository.deleteAllById(findProblem.choicesList.map { it.id })

        findProblem.choicesList.clear()
        findProblem.addChoices(choiceDataList)
        findProblem.updateFromDto(updateRequestDto)

        updateTags(findProblem, updateRequestDto.tags)

        return id
    }

    private fun setTags(problem: Problem, tagNames: List<String>) {
        val tags = tagRepository.findTagsByNameIn(tagNames)

        if (tags.isEmpty()) {
            throw ConditionConflictException(ErrorCode.CONDITION_NOT_FULFILLED, "태그의 개수는 1개 이상이여야합니다.")
        }

        val problemTags = tags.map {
            ProblemTag(problem = problem, tag = it)
        }

        problem.problemTags.addAll(problemTags)
    }

    private fun updateTags(problem: Problem, tagNames: MutableList<String>) {
        problem.problemTags.removeIf {
            if (it.tag.name !in tagNames) {
                problemTagRepository.delete(it)
                return@removeIf true
            }
            return@removeIf false
        }

        tagNames.removeIf {
            it in problem.problemTags.map { pt ->
                pt.tag.name
            }
        }

        val tags = tagRepository.findTagsByNameIn(tagNames)

        val problemTags = tags.map {
            ProblemTag(problem = problem, tag = it)
        }

        problem.problemTags.addAll(problemTags)
    }

    @Transactional
    override fun gradingLongProblem(
        email: String,
        problemId: Long,
        answer: String,
        isGrading: Boolean,
    ): LongProblemGradingHistoryDto {
        // get entities
        val findUser = userRepository.findByEmail(email)
            ?: throw EntityNotFoundException("$email 을 가진 유저는 존재하지 않습니다.")

        val findProblem = longProblemRepository.findByIdOrNull(problemId)
            ?: throw EntityNotFoundException("${problemId}번 문제는 존재하지 않는 서술형 문제입니다.")

        // check score
        val gradeResultDto = when (isGrading) {
            true -> getCorrectStandards(findProblem, answer)
            false -> GradeResultDto(
                correctKeywordIds = emptyList(),
            )
        }
        var userGradedScore = 0.0

        // get keywords
        val correctKeywordListDto = gradeResultDto.correctKeywordIds.map {
            val keyword = findProblem.gradingStandards.find { gs -> gs.id!! == it }
                ?: throw EntityNotFoundException("${it}번 채점 기준을 찾을 수 없습니다.")
            if (keyword.type != GradingStandardType.KEYWORD) {
                throw InternalServiceException(
                    ErrorCode.CONDITION_NOT_FULFILLED,
                    "${it}번 기준은 키워드 채점 기준이 아닙니다.",
                )
            }
            userGradedScore += keyword.score
            KeywordDto(
                keyword.id!!,
                keyword.content,
                true,
                gradeResultDto.predictKeywordPositions[it]
                    ?: throw EntityNotFoundException("키워드 위치를 찾을 수 없습니다."),
            )
        }.toList()

        val notCorrectKeywordListDto = findProblem.gradingStandards.filter {
            it.type == GradingStandardType.KEYWORD && it.id !in gradeResultDto.correctKeywordIds
        }.map {
            KeywordDto(it.id!!, it.content)
        }.toList()

        // get score from content standards
        val correctContentListDto = findProblem.gradingStandards.filter {
            it.id in gradeResultDto.correctContentIds
        }.map {
            if (it.type != GradingStandardType.CONTENT) {
                throw InternalServiceException(
                    ErrorCode.CONDITION_NOT_FULFILLED,
                    "${it.id}번 기준은 내용 채점 기준이 아닙니다.",
                )
            }
            userGradedScore += it.score
            ContentDto(it.id!!, it.content, true)
        }

        val notCorrectContentListDto = findProblem.gradingStandards.filter {
            it.type == GradingStandardType.CONTENT && it.id !in gradeResultDto.correctContentIds
        }.map {
            ContentDto(it.id!!, it.content)
        }.toList()

        if (correctContentListDto.size != gradeResultDto.correctContentIds.size) {
            throw EntityNotFoundException("채점 기준을 찾을 수 없습니다.")
        }

        // create user-answer
        val userAnswer = UserAnswer(answer = answer, problem = findProblem)
        userAnswerRepository.save(userAnswer)

        // create grading-history
        val gradingHistory = GradingHistory(
            problem = findProblem,
            user = findUser,
            userAnswer = answer,
            score = userGradedScore,
        )
        gradingHistoryRepository.save(gradingHistory)

        val standardAnswers = standardAnswerRepository.findAllByLongProblem(findProblem)
            .map { it.content } + findProblem.standardAnswer

        // create dto
        return LongProblemGradingHistoryDto.createDto(
            gradingHistoryId = gradingHistory.gradingHistoryId!!,
            problem = findProblem,
            userAnswer = answer,
            score = userGradedScore,
            keywords = correctKeywordListDto + notCorrectKeywordListDto,
            contents = correctContentListDto + notCorrectContentListDto,
            standardAnswer = standardAnswers.random(),
        )
    }

    @Transactional
    override fun gradingShortProblem(email: String, problemId: Long, answer: String): ShortProblemGradingHistoryDto {
        // get entities
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

    @Transactional
    override fun gradingMultipleChoiceProblem(
        email: String,
        problemId: Long,
        answerIds: List<Long>,
    ): MultipleChoiceProblemGradingHistoryDto {
        // get entities
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

    private fun getCorrectStandards(findProblem: LongProblem, answer: String): GradeResultDto {
        return if (findProblem.isGradable) {
            val gradingRequestDto = LongProblemGradingRequestToAiServerDto.createGradingRequestDto(findProblem, answer)
            val gradingResponseDto = aiServerClient.getGrade(gradingRequestDto)

            log.info("Integrate Grading response : {}", jacksonObjectMapper().writeValueAsString(gradingResponseDto))

            GradeResultDto(gradingResponseDto)
        } else {
            val gradingRequestDto = KeywordGradingRequestDto.createKeywordGradingRequestDto(findProblem, answer)
            val gradingResponseDto = aiServerClient.getKeywordGrade(gradingRequestDto)

            log.info("Keyword Grading response : {}", jacksonObjectMapper().writeValueAsString(gradingResponseDto))

            GradeResultDto(gradingResponseDto)
        }
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
