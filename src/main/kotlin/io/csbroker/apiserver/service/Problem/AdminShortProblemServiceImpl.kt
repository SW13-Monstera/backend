package io.csbroker.apiserver.service.Problem

import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.dto.problem.AdminProblemResponseDto
import io.csbroker.apiserver.dto.problem.AdminProblemSearchDto
import io.csbroker.apiserver.dto.problem.AdminProblemSearchResponseDto
import io.csbroker.apiserver.dto.problem.AdminUpsertRequestDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemSearchResponseDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemUpsertRequestDto
import io.csbroker.apiserver.repository.*
import org.springframework.data.repository.findByIdOrNull

class AdminShortProblemServiceImpl(
    private val shortProblemRepository: ShortProblemRepository,
    private val problemRepository: ProblemRepository,
    private val userRepository: UserRepository,
    private val tagRepository: TagRepository,
    private val problemTagRepository: ProblemTagRepository,
    ) : AdminProblemService {
    override fun findProblems(problemSearchDto: AdminProblemSearchDto): AdminProblemSearchResponseDto {
        val (id, title, description, pageable) = problemSearchDto
        val pagedProblems = shortProblemRepository
            .findShortProblemsByQuery(id, title, description, pageable)

        return ShortProblemSearchResponseDto(
            pagedProblems.map { it.toShortProblemDataDto() }.toList(),
            pagedProblems.totalPages,
            pagedProblems.totalElements,
        )
    }

    override fun findProblemById(id: Long, email: String?): AdminProblemResponseDto {
        val shortProblem = shortProblemRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("${id}번 문제는 존재하지 않는 단답형 문제입니다.")
        return shortProblem.toShortProblemResponseDto()
    }

    override fun createProblem(createRequestDto: AdminUpsertRequestDto, email: String): Long {
        val createRequestDto = createRequestDto as ShortProblemUpsertRequestDto
        val findUser = userRepository.findByEmail(email)
            ?: throw EntityNotFoundException("$email 을 가진 유저는 존재하지 않습니다.")
        val shortProblem = createRequestDto.toShortProblem(findUser)

        setTags(tagRepository, shortProblem, createRequestDto.tags)

        return problemRepository.save(shortProblem).id!!
    }

    override fun updateProblem(id: Long, updateRequestDto: AdminUpsertRequestDto, email: String): Long {
        val updateRequestDto = updateRequestDto as ShortProblemUpsertRequestDto
        val findProblem = shortProblemRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("${id}번 문제는 존재하지 않는 단답형 문제입니다.")

        updateTags(problemTagRepository, tagRepository, findProblem, updateRequestDto.tags)
        findProblem.updateFromDto(updateRequestDto)

        return id
    }
}
