package io.csbroker.apiserver.service.problem

import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.dto.problem.problemset.ProblemSetUpsertRequestDto
import io.csbroker.apiserver.model.ProblemSet
import io.csbroker.apiserver.model.ProblemSetMapping
import io.csbroker.apiserver.repository.problem.ProblemRepository
import io.csbroker.apiserver.repository.problem.ProblemSetMappingRepository
import io.csbroker.apiserver.repository.problem.ProblemSetRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ProblemSetServiceImpl(
    private val problemSetRepository: ProblemSetRepository,
    private val problemRepository: ProblemRepository,
    private val problemSetMappingRepository: ProblemSetMappingRepository,
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
        val problemSet = problemSetRepository.save(problemSetUpsertRequestDto.toProblemSet())
        createProblemSetMapping(problemSetUpsertRequestDto.problemIds, problemSet)
        return problemSet.id
    }

    @Transactional
    override fun updateProblemSet(id: Long, problemSetUpsertRequestDto: ProblemSetUpsertRequestDto): Long {
        val problemSet = findById(id)
        problemSet.updateContents(problemSetUpsertRequestDto.name, problemSetUpsertRequestDto.description)
        problemSetMappingRepository.deleteAllByProblemSetId(problemSet.id)
        createProblemSetMapping(problemSetUpsertRequestDto.problemIds, problemSet)
        return problemSet.id
    }

    private fun createProblemSetMapping(
        problemIds: List<Long>,
        problemSet: ProblemSet,
    ) {
        val problems = problemRepository.findAllById(problemIds)
        if (problems.size != problemIds.size) {
            throw EntityNotFoundException(
                "[ids = ${problemIds.joinToString(",")}] 에 해당하는 문제 목록을 찾을 수 없습니다.",
            )
        }

        val problemSetMappings = problems.map {
            ProblemSetMapping(
                problem = it,
                problemSet = problemSet,
            )
        }

        problemSetMappingRepository.saveAll(problemSetMappings)
    }
}
