package com.csbroker.apiserver.service

import com.csbroker.apiserver.common.client.AIServerClient
import com.csbroker.apiserver.common.enums.ErrorCode
import com.csbroker.apiserver.common.enums.GradingStandardType
import com.csbroker.apiserver.common.exception.ConditionConflictException
import com.csbroker.apiserver.common.exception.EntityNotFoundException
import com.csbroker.apiserver.common.util.log
import com.csbroker.apiserver.dto.problem.ProblemPageResponseDto
import com.csbroker.apiserver.dto.problem.ProblemSearchDto
import com.csbroker.apiserver.dto.problem.grade.GradingRequestDto
import com.csbroker.apiserver.dto.problem.longproblem.KeywordDto
import com.csbroker.apiserver.dto.problem.longproblem.LongProblemDetailResponseDto
import com.csbroker.apiserver.dto.problem.longproblem.LongProblemGradingHistoryDto
import com.csbroker.apiserver.dto.problem.longproblem.LongProblemResponseDto
import com.csbroker.apiserver.dto.problem.longproblem.LongProblemSearchResponseDto
import com.csbroker.apiserver.dto.problem.longproblem.LongProblemUpsertRequestDto
import com.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemDetailResponseDto
import com.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemGradingHistoryDto
import com.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemResponseDto
import com.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemSearchResponseDto
import com.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemUpsertRequestDto
import com.csbroker.apiserver.dto.problem.shortproblem.ShortProblemDetailResponseDto
import com.csbroker.apiserver.dto.problem.shortproblem.ShortProblemGradingHistoryDto
import com.csbroker.apiserver.dto.problem.shortproblem.ShortProblemResponseDto
import com.csbroker.apiserver.dto.problem.shortproblem.ShortProblemSearchResponseDto
import com.csbroker.apiserver.dto.problem.shortproblem.ShortProblemUpsertRequestDto
import com.csbroker.apiserver.model.GradingHistory
import com.csbroker.apiserver.model.Problem
import com.csbroker.apiserver.model.ProblemTag
import com.csbroker.apiserver.model.UserAnswer
import com.csbroker.apiserver.repository.ChoiceRepository
import com.csbroker.apiserver.repository.GradingHistoryRepository
import com.csbroker.apiserver.repository.GradingStandardRepository
import com.csbroker.apiserver.repository.LongProblemRepository
import com.csbroker.apiserver.repository.MultipleChoiceProblemRepository
import com.csbroker.apiserver.repository.ProblemRepository
import com.csbroker.apiserver.repository.ProblemTagRepository
import com.csbroker.apiserver.repository.ShortProblemRepository
import com.csbroker.apiserver.repository.TagRepository
import com.csbroker.apiserver.repository.UserAnswerRepository
import com.csbroker.apiserver.repository.UserRepository
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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
    private val aiServerClient: AIServerClient
) : ProblemService {

    override fun findProblems(problemSearchDto: ProblemSearchDto, pageable: Pageable): ProblemPageResponseDto {
        return ProblemPageResponseDto(this.problemRepository.findProblemsByQuery(problemSearchDto, pageable))
    }

    override fun findLongProblems(
        id: Long?,
        title: String?,
        description: String?,
        pageable: Pageable
    ): LongProblemSearchResponseDto {
        val pagedProblems = this.longProblemRepository
            .findLongProblemsByQuery(id, title, description, pageable)

        return LongProblemSearchResponseDto(
            pagedProblems.map { it.toLongProblemDataDto() }.toList(),
            pagedProblems.totalPages,
            pagedProblems.totalElements
        )
    }

    override fun findShortProblems(
        id: Long?,
        title: String?,
        description: String?,
        pageable: Pageable
    ): ShortProblemSearchResponseDto {
        val pagedProblems = this.shortProblemRepository
            .findShortProblemsByQuery(id, title, description, pageable)

        return ShortProblemSearchResponseDto(
            pagedProblems.map { it.toShortProblemDataDto() }.toList(),
            pagedProblems.totalPages,
            pagedProblems.totalElements
        )
    }

    override fun findMultipleProblems(
        id: Long?,
        title: String?,
        description: String?,
        pageable: Pageable
    ): MultipleChoiceProblemSearchResponseDto {
        val pagedProblems = this.multipleChoiceProblemRepository
            .findMultipleChoiceProblemsByQuery(id, title, description, pageable)

        return MultipleChoiceProblemSearchResponseDto(
            pagedProblems.map { it.toMultipleChoiceDataDto() }.toList(),
            pagedProblems.totalPages,
            pagedProblems.totalElements
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

    override fun findMultipleChoiceProblemDetailById(id: Long, email: String?): MultipleChoiceProblemDetailResponseDto {
        return multipleChoiceProblemRepository.findByIdOrNull(id)?.toDetailResponseDto(email)
            ?: throw EntityNotFoundException("${id}번 문제를 찾을 수 없습니다.")
    }

    override fun findLongProblemById(id: Long): LongProblemResponseDto {
        val longProblem = this.longProblemRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("${id}번 문제는 존재하지 않는 서술형 문제입니다.")
        return longProblem.toLongProblemResponseDto()
    }

    override fun findShortProblemById(id: Long): ShortProblemResponseDto {
        val shortProblem = this.shortProblemRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("${id}번 문제는 존재하지 않는 단답형 문제입니다.")
        return shortProblem.toShortProblemResponseDto()
    }

    override fun findMultipleProblemById(id: Long): MultipleChoiceProblemResponseDto {
        val multipleChoiceProblem = this.multipleChoiceProblemRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("${id}번 문제는 존재하지 않는 객관식 문제입니다.")
        return multipleChoiceProblem.toMultipleChoiceProblemResponseDto()
    }

    @Transactional
    override fun removeProblemById(id: Long) {
        this.problemRepository.deleteById(id)
    }

    @Transactional
    override fun removeProblemsById(ids: List<Long>) {
        this.problemRepository.deleteProblemsByIdIn(ids)
    }

    @Transactional
    override fun createLongProblem(createRequestDto: LongProblemUpsertRequestDto, email: String): Long {
        val findUser = this.userRepository.findByEmail(email)
            ?: throw EntityNotFoundException("$email 을 가진 유저는 존재하지 않습니다.")
        val longProblem = createRequestDto.toLongProblem(findUser)
        val gradingStandardList = createRequestDto.getGradingStandardList(longProblem)

        longProblem.addGradingStandards(gradingStandardList)
        this.setTags(longProblem, createRequestDto.tags)

        return this.problemRepository.save(longProblem).id!!
    }

    @Transactional
    override fun createShortProblem(createRequestDto: ShortProblemUpsertRequestDto, email: String): Long {
        val findUser = this.userRepository.findByEmail(email)
            ?: throw EntityNotFoundException("$email 을 가진 유저는 존재하지 않습니다.")
        val shortProblem = createRequestDto.toShortProblem(findUser)

        this.setTags(shortProblem, createRequestDto.tags)

        return this.problemRepository.save(shortProblem).id!!
    }

    @Transactional
    override fun createMultipleChoiceProblem(
        createRequestDto: MultipleChoiceProblemUpsertRequestDto,
        email: String
    ): Long {
        val findUser = this.userRepository.findByEmail(email)
            ?: throw EntityNotFoundException("$email 을 가진 유저는 존재하지 않습니다.")

        val multipleChoiceProblem = createRequestDto.toMultipleChoiceProblem(findUser)
        val choiceDataList = createRequestDto.getChoiceList(multipleChoiceProblem)

        this.setTags(multipleChoiceProblem, createRequestDto.tags)

        if (choiceDataList.count { it.isAnswer } == 0) {
            throw ConditionConflictException(ErrorCode.CONDITION_NOT_FULFILLED, "답의 개수는 1개 이상이여야합니다.")
        }

        multipleChoiceProblem.addChoices(choiceDataList)

        return this.problemRepository.save(multipleChoiceProblem).id!!
    }

    @Transactional
    override fun updateLongProblem(id: Long, updateRequestDto: LongProblemUpsertRequestDto, email: String): Long {
        val findProblem = this.longProblemRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("${id}번 문제는 존재하지 않는 서술형 문제입니다.")

        findProblem.gradingStandards.forEach {
            this.gradingStandardRepository.delete(it)
        }

        findProblem.gradingStandards.clear()

        val gradingStandardList = updateRequestDto.getGradingStandardList(findProblem)

        findProblem.addGradingStandards(gradingStandardList)

        findProblem.updateFromDto(updateRequestDto)

        this.updateTags(findProblem, updateRequestDto.tags)

        return id
    }

    @Transactional
    override fun updateShortProblem(id: Long, updateRequestDto: ShortProblemUpsertRequestDto, email: String): Long {
        val findProblem = this.shortProblemRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("${id}번 문제는 존재하지 않는 단답형 문제입니다.")

        this.updateTags(findProblem, updateRequestDto.tags)
        findProblem.updateFromDto(updateRequestDto)

        return id
    }

    @Transactional
    override fun updateMultipleChoiceProblem(
        id: Long,
        updateRequestDto: MultipleChoiceProblemUpsertRequestDto,
        email: String
    ): Long {
        val findProblem = this.multipleChoiceProblemRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("${id}번 문제는 존재하지 않는 객관식 문제입니다.")

        val choiceDataList = updateRequestDto.getChoiceList(findProblem)

        if (choiceDataList.count { it.isAnswer } == 0) {
            throw ConditionConflictException(ErrorCode.CONDITION_NOT_FULFILLED, "답의 개수는 1개 이상이여야합니다.")
        }

        findProblem.choicesList.forEach {
            this.choiceRepository.delete(it)
        }
        findProblem.choicesList.clear()
        findProblem.addChoices(choiceDataList)
        findProblem.updateFromDto(updateRequestDto)

        this.updateTags(findProblem, updateRequestDto.tags)

        return id
    }

    private fun setTags(problem: Problem, tagNames: List<String>) {
        val tags = this.tagRepository.findTagsByNameIn(tagNames)

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
                this.problemTagRepository.delete(it)
                return@removeIf true
            }
            return@removeIf false
        }

        tagNames.removeIf {
            it in problem.problemTags.map { pt ->
                pt.tag.name
            }
        }

        val tags = this.tagRepository.findTagsByNameIn(tagNames)

        val problemTags = tags.map {
            ProblemTag(problem = problem, tag = it)
        }

        problem.problemTags.addAll(problemTags)
    }

    @Transactional
    override fun gradingLongProblem(
        email: String,
        problemId: Long,
        answer: String
    ): LongProblemGradingHistoryDto {
        // get entities
        val findUser = this.userRepository.findByEmail(email)
            ?: throw EntityNotFoundException("$email 을 가진 유저는 존재하지 않습니다.")

        val findProblem = this.longProblemRepository.findByIdOrNull(problemId)
            ?: throw EntityNotFoundException("${problemId}번 문제는 존재하지 않는 서술형 문제입니다.")

        // check score
        val gradingRequestDto = GradingRequestDto.createGradingRequestDto(findProblem, answer)

        val gradingResponseDto = this.aiServerClient.getGrade(gradingRequestDto)

        log.info("Grading response : {}", jacksonObjectMapper().writeValueAsString(gradingResponseDto))

        val correctKeywordIds = gradingResponseDto.correct_keywords.map { it.id }
        val correctPromptIds = gradingResponseDto.correct_contents.map { it.id }
        var userGradedScore = 0.0

        // get keywords
        val correctKeywordListDto = gradingResponseDto.correct_keywords.map {
            val keyword = findProblem.gradingStandards.find { gs -> gs.id!! == it.id }
                ?: throw EntityNotFoundException("${it.id}번 채점 기준을 찾을 수 없습니다.")
            userGradedScore += keyword.score
            KeywordDto(
                keyword.id!!,
                keyword.content,
                true,
                it.predict_keyword_position
            )
        }.toList()

        val notCorrectKeywordListDto = findProblem.gradingStandards.filter {
            it.type == GradingStandardType.KEYWORD && it.id !in correctKeywordIds
        }.map {
            KeywordDto(it.id!!, it.content)
        }.toList()

        // get score from prompts
        val promptScores = findProblem.gradingStandards.filter {
            it.type == GradingStandardType.PROMPT && it.id in correctPromptIds
        }.map {
            it.score
        }

        if (promptScores.size != correctPromptIds.size) {
            throw EntityNotFoundException("채점 기준을 찾을 수 없습니다.")
        }

        userGradedScore += promptScores.sum()

        // create user-answer
        val userAnswer = UserAnswer(answer = answer, problem = findProblem)
        this.userAnswerRepository.save(userAnswer)

        // create grading-history
        val gradingHistory = GradingHistory(
            problem = findProblem,
            user = findUser,
            userAnswer = answer,
            score = userGradedScore
        )
        this.gradingHistoryRepository.save(gradingHistory)

        // create dto
        return LongProblemGradingHistoryDto.createDto(
            gradingHistoryId = gradingHistory.gradingHistoryId!!,
            problem = findProblem,
            userAnswer = answer,
            score = userGradedScore,
            keywords = correctKeywordListDto + notCorrectKeywordListDto
        )
    }

    @Transactional
    override fun gradingShortProblem(email: String, problemId: Long, answer: String): ShortProblemGradingHistoryDto {
        // get entities
        val findUser = this.userRepository.findByEmail(email)
            ?: throw EntityNotFoundException("$email 을 가진 유저는 존재하지 않습니다.")

        val findProblem = this.shortProblemRepository.findByIdOrNull(problemId)
            ?: throw EntityNotFoundException("${problemId}번 문제는 존재하지 않는 서술형 문제입니다.")

        // check score
        val isAnswer = findProblem.answer == answer
        val score = if (isAnswer) findProblem.score else 0.0

        // create grading-history
        val gradingHistory = GradingHistory(
            problem = findProblem,
            user = findUser,
            userAnswer = answer,
            score = score
        )
        this.gradingHistoryRepository.save(gradingHistory)

        // create dto
        return ShortProblemGradingHistoryDto.createDto(
            gradingHistoryId = gradingHistory.gradingHistoryId!!,
            problem = findProblem,
            userAnswer = answer,
            score = score,
            isAnswer = isAnswer
        )
    }

    @Transactional
    override fun gradingMultipleChoiceProblem(
        email: String,
        problemId: Long,
        answerIds: List<Long>
    ): MultipleChoiceProblemGradingHistoryDto {
        // get entities
        val findUser = this.userRepository.findByEmail(email)
            ?: throw EntityNotFoundException("$email 을 가진 유저는 존재하지 않습니다.")

        val findProblem = this.multipleChoiceProblemRepository.findByIdOrNull(problemId)
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
            score = score
        )
        this.gradingHistoryRepository.save(gradingHistory)

        // create dto
        return MultipleChoiceProblemGradingHistoryDto.createDto(
            gradingHistoryId = gradingHistory.gradingHistoryId!!,
            problem = findProblem,
            userAnswerIds = answerIds,
            score = score,
            isAnswer = isAnswer
        )
    }
}
