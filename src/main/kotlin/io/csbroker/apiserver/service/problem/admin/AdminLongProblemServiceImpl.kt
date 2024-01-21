package io.csbroker.apiserver.service.problem.admin

import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.dto.problem.AdminProblemSearchDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemResponseDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemSearchResponseDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemUpsertRequestDto
import io.csbroker.apiserver.model.StandardAnswer
import io.csbroker.apiserver.model.User
import io.csbroker.apiserver.repository.problem.GradingStandardRepository
import io.csbroker.apiserver.repository.problem.LongProblemRepository
import io.csbroker.apiserver.repository.problem.ProblemRepository
import io.csbroker.apiserver.repository.problem.StandardAnswerRepository
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
    private val standardAnswerRepository: StandardAnswerRepository,
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
    override fun createProblem(createRequestDto: LongProblemUpsertRequestDto, user: User): Long {
        val longProblem = createRequestDto.toLongProblem(user)
        val gradingStandardList = createRequestDto.getGradingStandardList(longProblem)
        longProblem.addGradingStandards(gradingStandardList)

        val savedProblem = problemRepository.save(longProblem)
        tagUpserter.setTags(savedProblem, createRequestDto.tags)

        standardAnswerRepository.saveAll(
            createRequestDto.standardAnswers.map {
                StandardAnswer(
                    content = it,
                    longProblem = savedProblem,
                )
            },
        )

        return savedProblem.id
    }

    @Transactional
    override fun updateProblem(id: Long, updateRequestDto: LongProblemUpsertRequestDto): Long {
        val longProblem = longProblemRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("${id}번 문제는 존재하지 않는 서술형 문제입니다.")
        val gradingStandardList = updateRequestDto.getGradingStandardList(longProblem)

        if (longProblem.gradingStandards.map { it.content }.toSet() != gradingStandardList.map { it.content }.toSet()) {
            gradingStandardRepository.deleteAllById(longProblem.gradingStandards.map { it.id })
            longProblem.addGradingStandards(gradingStandardList)
        }

        longProblem.updateFromDto(updateRequestDto)
        tagUpserter.updateTags(longProblem, updateRequestDto.tags.toMutableList())
        return id
    }
}
