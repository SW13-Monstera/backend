package io.csbroker.apiserver.service.Problem

import io.csbroker.apiserver.common.enums.ErrorCode
import io.csbroker.apiserver.common.exception.ConditionConflictException
import io.csbroker.apiserver.dto.problem.AdminUpsertRequestDto
import io.csbroker.apiserver.dto.problem.AdminProblemResponseDto
import io.csbroker.apiserver.dto.problem.AdminProblemSearchDto
import io.csbroker.apiserver.dto.problem.AdminProblemSearchResponseDto
import io.csbroker.apiserver.model.Problem
import io.csbroker.apiserver.model.ProblemTag
import io.csbroker.apiserver.repository.ProblemTagRepository
import io.csbroker.apiserver.repository.TagRepository

interface AdminProblemService {

    fun findProblems(problemSearchDto : AdminProblemSearchDto) : AdminProblemSearchResponseDto
    fun findProblemById(id: Long) : AdminProblemResponseDto
    fun createProblem(createRequestDto: AdminUpsertRequestDto, email: String) : Long
    fun updateProblem(id: Long, updateRequestDto: AdminUpsertRequestDto, email: String) : Long

    fun setTags(tagRepository: TagRepository, problem: Problem, tagNames: List<String>) {
        val tags = tagRepository.findTagsByNameIn(tagNames)

        if (tags.isEmpty()) {
            throw ConditionConflictException(ErrorCode.CONDITION_NOT_FULFILLED, "태그의 개수는 1개 이상이여야합니다.")
        }

        val problemTags = tags.map {
            ProblemTag(problem = problem, tag = it)
        }

        problem.problemTags.addAll(problemTags)
    }

    fun updateTags(
        problemTagRepository: ProblemTagRepository,
        tagRepository: TagRepository,
        problem: Problem,
        tagNames: MutableList<String>
    ) {
        problem.problemTags.removeIf {
            if (it.tag.name !in tagNames) {
                problemTagRepository.delete(it)
                return@removeIf true
            }
            return@removeIf false
        }

        tagNames.removeIf {
            it in problem.problemTags.map { pt ->
                pt.tag.name
            }
        }

        val tags = tagRepository.findTagsByNameIn(tagNames)

        val problemTags = tags.map {
            ProblemTag(problem = problem, tag = it)
        }

        problem.problemTags.addAll(problemTags)
    }

}
