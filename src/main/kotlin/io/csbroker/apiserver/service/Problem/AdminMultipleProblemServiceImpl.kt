package io.csbroker.apiserver.service.Problem

import io.csbroker.apiserver.common.enums.ErrorCode
import io.csbroker.apiserver.common.exception.ConditionConflictException
import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.dto.problem.AdminProblemResponseDto
import io.csbroker.apiserver.dto.problem.AdminProblemSearchDto
import io.csbroker.apiserver.dto.problem.AdminProblemSearchResponseDto
import io.csbroker.apiserver.dto.problem.AdminUpsertRequestDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemSearchResponseDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemUpsertRequestDto
import io.csbroker.apiserver.repository.*
import org.springframework.data.repository.findByIdOrNull

class AdminMultipleProblemServiceImpl(
    private val multipleChoiceProblemRepository: MultipleChoiceProblemRepository,
    private val choiceRepository: ChoiceRepository,
    private val problemRepository: ProblemRepository,
    private val userRepository: UserRepository,
    private val tagRepository: TagRepository,
    private val problemTagRepository: ProblemTagRepository,
) : AdminProblemService {
    override fun findProblems(problemSearchDto: AdminProblemSearchDto): AdminProblemSearchResponseDto {
        val (id, title, description, pageable) = problemSearchDto
        val pagedProblems = multipleChoiceProblemRepository
            .findMultipleChoiceProblemsByQuery(id, title, description, pageable)

        return MultipleChoiceProblemSearchResponseDto(
            pagedProblems.map { it.toMultipleChoiceDataDto() }.toList(),
            pagedProblems.totalPages,
            pagedProblems.totalElements,
        )
    }

    override fun findProblemById(id: Long, email: String?): AdminProblemResponseDto {
        val multipleChoiceProblem = multipleChoiceProblemRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("${id}번 문제는 존재하지 않는 객관식 문제입니다.")
        return multipleChoiceProblem.toMultipleChoiceProblemResponseDto()
    }

    override fun createProblem(createRequestDto: AdminUpsertRequestDto, email: String): Long {
        val createRequestDto = createRequestDto as MultipleChoiceProblemUpsertRequestDto
        val findUser = userRepository.findByEmail(email)
            ?: throw EntityNotFoundException("$email 을 가진 유저는 존재하지 않습니다.")

        val multipleChoiceProblem = createRequestDto.toMultipleChoiceProblem(findUser)
        val choiceDataList = createRequestDto.getChoiceList(multipleChoiceProblem)

        setTags(tagRepository, multipleChoiceProblem, createRequestDto.tags)

        if (choiceDataList.count { it.isAnswer } == 0) {
            throw ConditionConflictException(ErrorCode.CONDITION_NOT_FULFILLED, "답의 개수는 1개 이상이여야합니다.")
        }

        multipleChoiceProblem.addChoices(choiceDataList)

        return problemRepository.save(multipleChoiceProblem).id!!
    }

    override fun updateProblem(id: Long, updateRequestDto: AdminUpsertRequestDto, email: String): Long {
        val updateRequestDto = updateRequestDto as MultipleChoiceProblemUpsertRequestDto
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

        updateTags(problemTagRepository, tagRepository, findProblem, updateRequestDto.tags)

        return id
    }
}
