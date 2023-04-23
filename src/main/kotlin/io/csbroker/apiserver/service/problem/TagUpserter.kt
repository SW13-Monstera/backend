package io.csbroker.apiserver.service.problem

import io.csbroker.apiserver.common.enums.ErrorCode
import io.csbroker.apiserver.common.exception.ConditionConflictException
import io.csbroker.apiserver.model.Problem
import io.csbroker.apiserver.model.ProblemTag
import io.csbroker.apiserver.repository.problem.ProblemTagRepository
import io.csbroker.apiserver.repository.problem.TagRepository
import org.springframework.stereotype.Component

@Component
class TagUpserter(
    private val tagRepository: TagRepository,
    private val problemTagRepository: ProblemTagRepository,
) {
    fun setTags(problem: Problem, tagNames: List<String>) {
        val tags = tagRepository.findTagsByNameIn(tagNames)

        if (tags.isEmpty()) {
            throw ConditionConflictException(ErrorCode.CONDITION_NOT_FULFILLED, "태그의 개수는 1개 이상이여야합니다.")
        }

        val problemTags = tags.map {
            ProblemTag(problem = problem, tag = it)
        }

        problemTagRepository.saveAll(problemTags)
        problem.problemTags.addAll(problemTags)
    }

    fun updateTags(problem: Problem, tagNames: MutableList<String>) {
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
