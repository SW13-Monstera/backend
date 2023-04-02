package io.csbroker.apiserver.service.Problem

import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.dto.problem.AdminProblemResponseDto
import io.csbroker.apiserver.dto.problem.AdminProblemSearchDto
import io.csbroker.apiserver.dto.problem.AdminProblemSearchResponseDto
import io.csbroker.apiserver.dto.problem.AdminUpsertRequestDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemSearchResponseDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemUpsertRequestDto
import io.csbroker.apiserver.repository.*
import org.springframework.data.repository.findByIdOrNull

class AdminLongProblemServiceImpl(
    private val longProblemRepository: LongProblemRepository,
    private val problemRepository: ProblemRepository,
    private val userRepository: UserRepository,
    private val tagRepository: TagRepository,
    private val gradingStandardRepository: GradingStandardRepository,
    private val problemTagRepository: ProblemTagRepository,
    ) : AdminProblemService {
    override fun findProblems(problemSearchDto: AdminProblemSearchDto): AdminProblemSearchResponseDto {
        val (id, title, description, pageable) = problemSearchDto
        val pagedProblems = longProblemRepository
            .findLongProblemsByQuery(id, title, description, pageable)

        return LongProblemSearchResponseDto(
            pagedProblems.map { it.toLongProblemDataDto() }.toList(),
            pagedProblems.totalPages,
            pagedProblems.totalElements,
        )
    }

    override fun findProblemById(id: Long, email: String?): AdminProblemResponseDto {
        val longProblem = longProblemRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("${id}번 문제는 존재하지 않는 서술형 문제입니다.")
        return longProblem.toLongProblemResponseDto()
    }

    override fun createProblem(createRequestDto: AdminUpsertRequestDto, email: String): Long {
        val createRequestDto = createRequestDto as LongProblemUpsertRequestDto // 예외 처리
        val findUser = userRepository.findByEmail(email)
            ?: throw EntityNotFoundException("$email 을 가진 유저는 존재하지 않습니다.")
        val longProblem = createRequestDto.toLongProblem(findUser)
        val gradingStandardList = createRequestDto.getGradingStandardList(longProblem)

        longProblem.addGradingStandards(gradingStandardList)
        setTags(tagRepository, longProblem, createRequestDto.tags)

        return problemRepository.save(longProblem).id!!
    }

    override fun updateProblem(id: Long, updateRequestDto: AdminUpsertRequestDto, email: String): Long {
        val updateRequestDto = updateRequestDto as LongProblemUpsertRequestDto
        val findProblem = longProblemRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("${id}번 문제는 존재하지 않는 서술형 문제입니다.")

        gradingStandardRepository.deleteAllById(findProblem.gradingStandards.map { it.id })

        findProblem.gradingStandards.clear()

        val gradingStandardList = updateRequestDto.getGradingStandardList(findProblem)

        findProblem.addGradingStandards(gradingStandardList)

        findProblem.updateFromDto(updateRequestDto)

        updateTags(problemTagRepository, tagRepository, findProblem, updateRequestDto.tags)

        return id
    }
}
