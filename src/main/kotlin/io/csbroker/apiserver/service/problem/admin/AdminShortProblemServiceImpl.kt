package io.csbroker.apiserver.service.problem.admin

import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.dto.problem.AdminProblemSearchDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemResponseDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemSearchResponseDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemUpsertRequestDto
import io.csbroker.apiserver.model.User
import io.csbroker.apiserver.repository.problem.ProblemRepository
import io.csbroker.apiserver.repository.problem.ShortProblemRepository
import io.csbroker.apiserver.repository.user.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AdminShortProblemServiceImpl(
    private val shortProblemRepository: ShortProblemRepository,
    private val problemRepository: ProblemRepository,
    private val userRepository: UserRepository,
    private val tagUpserter: TagUpserter,
) : AdminShortProblemService {
    override fun findProblems(problemSearchDto: AdminProblemSearchDto): ShortProblemSearchResponseDto {
        val (id, title, description, pageable) = problemSearchDto
        val pagedProblems = shortProblemRepository
            .findShortProblemsByQuery(id, title, description, pageable)

        return ShortProblemSearchResponseDto(
            pagedProblems.map { it.toShortProblemDataDto() }.toList(),
            pagedProblems.totalPages,
            pagedProblems.totalElements,
        )
    }

    override fun findProblemById(id: Long): ShortProblemResponseDto {
        val shortProblem = shortProblemRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("${id}번 문제는 존재하지 않는 단답형 문제입니다.")
        return shortProblem.toShortProblemResponseDto()
    }

    @Transactional
    override fun createProblem(createRequestDto: ShortProblemUpsertRequestDto, user: User): Long {
        val shortProblem = createRequestDto.toShortProblem(user)
        tagUpserter.setTags(shortProblem, createRequestDto.tags)
        return problemRepository.save(shortProblem).id!!
    }

    @Transactional
    override fun updateProblem(id: Long, updateRequestDto: ShortProblemUpsertRequestDto): Long {
        val findProblem = shortProblemRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("${id}번 문제는 존재하지 않는 단답형 문제입니다.")

        tagUpserter.updateTags(findProblem, updateRequestDto.tags)
        findProblem.updateFromDto(updateRequestDto)

        return id
    }
}
