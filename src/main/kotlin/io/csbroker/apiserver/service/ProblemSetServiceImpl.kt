package io.csbroker.apiserver.service

import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.dto.problem.problemset.ProblemSetUpsertRequestDto
import io.csbroker.apiserver.model.ProblemSet
import io.csbroker.apiserver.repository.ProblemRepository
import io.csbroker.apiserver.repository.ProblemSetRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ProblemSetServiceImpl(
    private val problemSetRepository: ProblemSetRepository,
    private val problemRepository: ProblemRepository,
) : ProblemSetService {
    override fun findAll(): List<ProblemSet> {
        return problemSetRepository.findAll()
    }

    override fun findById(id: Long): ProblemSet {
        return problemSetRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("$id 에 해당하는 문제 세트를 찾을 수 없습니다.")
    }

    @Transactional
    override fun createProblemSet(problemSetUpsertRequestDto: ProblemSetUpsertRequestDto): Long {
        val (problemIds, name, description) = problemSetUpsertRequestDto
        val problems = problemRepository.findAllById(problemIds)

        if (problems.size != problemIds.size) {
            throw EntityNotFoundException(
                "[ids = ${problemIds.joinToString(",")}] 에 해당하는 문제 목록을 찾을 수 없습니다.",
            )
        }

        val problemSet = ProblemSet(
            name = name,
            description = description,
            problems = problems,
        )

        return problemSetRepository.save(problemSet).id!!
    }

    @Transactional
    override fun updateProblemSet(id: Long, problemSetUpsertRequestDto: ProblemSetUpsertRequestDto): Long {
        val (problemIds, name, description) = problemSetUpsertRequestDto
        val problemSet = findById(id)
        val problems = problemRepository.findAllById(problemIds)

        if (problems.size != problemIds.size) {
            throw EntityNotFoundException(
                "[ids = ${problemIds.joinToString(",")}] 에 해당하는 문제 목록을 찾을 수 없습니다.",
            )
        }

        problemSet.update(name, description, problems)

        return problemSet.id!!
    }
}
