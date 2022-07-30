package com.csbroker.apiserver.service

import com.csbroker.apiserver.dto.problem.LongProblemUpsertRequestDto
import com.csbroker.apiserver.dto.problem.MultipleChoiceProblemUpsertRequestDto
import com.csbroker.apiserver.dto.problem.ProblemDetailResponseDto
import com.csbroker.apiserver.dto.problem.ProblemResponseDto
import com.csbroker.apiserver.dto.problem.ProblemSearchDto
import com.csbroker.apiserver.dto.problem.ShortProblemUpsertRequestDto
import com.csbroker.apiserver.model.Problem
import com.csbroker.apiserver.model.ProblemTag
import com.csbroker.apiserver.repository.ChoiceRepository
import com.csbroker.apiserver.repository.GradingStandardRepository
import com.csbroker.apiserver.repository.LongProblemRepository
import com.csbroker.apiserver.repository.MultipleChoiceProblemRepository
import com.csbroker.apiserver.repository.ProblemRepository
import com.csbroker.apiserver.repository.ProblemTagRepository
import com.csbroker.apiserver.repository.ShortProblemRepository
import com.csbroker.apiserver.repository.TagRepository
import com.csbroker.apiserver.repository.UserRepository
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
    private val gradingStandardRepository: GradingStandardRepository
) : ProblemService {

    override fun findProblems(problemSearchDto: ProblemSearchDto, pageable: Pageable): List<ProblemResponseDto> {
        return this.problemRepository.findProblemsByQuery(problemSearchDto, pageable)
            .map(Problem::toProblemResponseDto)
    }

    override fun findProblemById(id: Long): ProblemDetailResponseDto? {
        return problemRepository.findByIdOrNull(id)?.toProblemDetailResponseDto()
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
        val findUser = this.userRepository.findByEmail(email) ?: throw IllegalArgumentException("에러 발생")
        val longProblem = createRequestDto.toLongProblem(findUser)
        val gradingStandardList = createRequestDto.getGradingStandardList(longProblem)

        longProblem.addGradingStandards(gradingStandardList)
        this.setTags(longProblem, createRequestDto.tags)

        return this.problemRepository.save(longProblem).id!!
    }

    @Transactional
    override fun createShortProblem(createRequestDto: ShortProblemUpsertRequestDto, email: String): Long {
        val findUser = userRepository.findByEmail(email) ?: throw IllegalArgumentException("에러 발생")
        val shortProblem = createRequestDto.toShortProblem(findUser)

        this.setTags(shortProblem, createRequestDto.tags)

        return this.problemRepository.save(shortProblem).id!!
    }

    @Transactional
    override fun createMultipleChoiceProblem(
        createRequestDto: MultipleChoiceProblemUpsertRequestDto,
        email: String
    ): Long {
        val findUser = userRepository.findByEmail(email) ?: throw IllegalArgumentException("에러 발생")
        val multipleChoiceProblem = createRequestDto.toMultipleChoiceProblem(findUser)
        val choiceDataList = createRequestDto.getChoiceList(multipleChoiceProblem)

        this.setTags(multipleChoiceProblem, createRequestDto.tags)

        if (choiceDataList.count { it.isAnswer } == 0) {
            throw IllegalArgumentException("답의 개수는 1개 이상이여야합니다.")
        }

        multipleChoiceProblem.addChoices(choiceDataList)

        return this.problemRepository.save(multipleChoiceProblem).id!!
    }

    @Transactional
    override fun updateLongProblem(id: Long, updateRequestDto: LongProblemUpsertRequestDto, email: String): Long {
        val findProblem = this.longProblemRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("에러 발생")

        findProblem.gradingStandards.forEach {
            this.gradingStandardRepository.delete(it)
        }

        findProblem.gradingStandards.clear()

        val gradingStandardList = updateRequestDto.getGradingStandardList(findProblem)

        findProblem.addGradingStandards(gradingStandardList)

        this.updateTags(findProblem, updateRequestDto.tags)

        return id
    }

    @Transactional
    override fun updateShortProblem(id: Long, updateRequestDto: ShortProblemUpsertRequestDto, email: String): Long {
        val findProblem = this.shortProblemRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("에러 발생")

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
            ?: throw IllegalArgumentException("에러 발생")
        val choiceDataList = updateRequestDto.getChoiceList(findProblem)

        if (choiceDataList.count { it.isAnswer } == 0) {
            throw IllegalArgumentException("답의 개수는 1개 이상이여야합니다.")
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
            throw IllegalArgumentException("에러 발생")
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
}
