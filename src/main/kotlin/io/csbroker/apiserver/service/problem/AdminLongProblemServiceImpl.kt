package io.csbroker.apiserver.service.problem

import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.dto.problem.AdminProblemSearchDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemResponseDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemSearchResponseDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemUpsertRequestDto
import io.csbroker.apiserver.repository.problem.GradingStandardRepository
import io.csbroker.apiserver.repository.problem.LongProblemRepository
import io.csbroker.apiserver.repository.problem.ProblemRepository
import io.csbroker.apiserver.repository.user.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AdminLongProblemServiceImpl(
    private val longProblemRepository: LongProblemRepository,
    private val problemRepository: ProblemRepository,
    private val userRepository: UserRepository,
    private val gradingStandardRepository: GradingStandardRepository,
    private val tagUpserter: TagUpserter,
) : AdminLongProblemService {
    override fun findProblems(problemSearchDto: AdminProblemSearchDto): LongProblemSearchResponseDto {
        val (id, title, description, pageable) = problemSearchDto
        val pagedProblems = longProblemRepository
            .findLongProblemsByQuery(id, title, description, pageable)

        return LongProblemSearchResponseDto(
            pagedProblems.map { it.toLongProblemDataDto() }.toList(),
            pagedProblems.totalPages,
            pagedProblems.totalElements,
        )
    }

    override fun findProblemById(id: Long): LongProblemResponseDto {
        val longProblem = longProblemRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("${id}번 문제는 존재하지 않는 서술형 문제입니다.")
        return longProblem.toLongProblemResponseDto()
    }

    @Transactional
    override fun createProblem(createRequestDto: LongProblemUpsertRequestDto, email: String): Long {
        val findUser = userRepository.findByEmail(email)
            ?: throw EntityNotFoundException("$email 을 가진 유저는 존재하지 않습니다.")
        val longProblem = createRequestDto.toLongProblem(findUser)
        val gradingStandardList = createRequestDto.getGradingStandardList(longProblem)

        longProblem.addGradingStandards(gradingStandardList)
        tagUpserter.setTags(longProblem, createRequestDto.tags)

        return problemRepository.save(longProblem).id!!
    }

    @Transactional
    override fun updateProblem(id: Long, updateRequestDto: LongProblemUpsertRequestDto, email: String): Long {
        val findProblem = longProblemRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("${id}번 문제는 존재하지 않는 서술형 문제입니다.")

        gradingStandardRepository.deleteAllById(findProblem.gradingStandards.map { it.id })

        findProblem.gradingStandards.clear()

        val gradingStandardList = updateRequestDto.getGradingStandardList(findProblem)

        findProblem.addGradingStandards(gradingStandardList)

        findProblem.updateFromDto(updateRequestDto)

        tagUpserter.updateTags(findProblem, updateRequestDto.tags)

        return id
    }
}
