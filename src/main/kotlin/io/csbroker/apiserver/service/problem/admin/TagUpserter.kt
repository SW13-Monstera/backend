package io.csbroker.apiserver.service.problem.admin

import io.csbroker.apiserver.common.enums.ErrorCode
import io.csbroker.apiserver.common.exception.ConditionConflictException
import io.csbroker.apiserver.model.Problem
import io.csbroker.apiserver.model.ProblemTag
import io.csbroker.apiserver.model.Tag
import io.csbroker.apiserver.repository.problem.ProblemTagRepository
import io.csbroker.apiserver.repository.problem.TagRepository
import org.springframework.stereotype.Component

@Component
class TagUpserter(
    private val tagRepository: TagRepository,
    private val problemTagRepository: ProblemTagRepository,
) {
    fun setTags(problem: Problem, tagNames: List<String>) {
        if (tagNames.isEmpty()) {
            throw ConditionConflictException(ErrorCode.CONDITION_NOT_FULFILLED, "태그의 개수는 1개 이상이여야합니다.")
        }
        if (problem.problemTags.isNotEmpty()) {
            throw ConditionConflictException(ErrorCode.CONDITION_NOT_FULFILLED, "태그가 이미 존재합니다.")
        }
        val tags = tagRepository.findTagsByNameIn(tagNames)

        checkEveryTagExist(tags, tagNames)
        problem.addTags(tags)
    }

    fun updateTags(problem: Problem, tagNames: MutableList<String>) {
        if (isNotChanged(problem, tagNames)) return

        problem.problemTags.clear()
        val tags = tagRepository.findTagsByNameIn(tagNames)
        checkEveryTagExist(tags, tagNames)

        val problemTags = tags.map { ProblemTag(problem = problem, tag = it) }
        problem.problemTags.addAll(problemTags)
    }

    private fun isNotChanged(
        problem: Problem,
        tagNames: MutableList<String>,
    ) = problem.problemTags.map { it.tag.name }.toSet() == tagNames.toSet()

    private fun checkEveryTagExist(
        existTags: List<Tag>,
        tagNames: List<String>,
    ) {
        if (existTags.size != tagNames.size) {
            val notExistTags = tagNames.filter {
                it !in existTags.map { tag -> tag.name }
            }
            throw ConditionConflictException(
                ErrorCode.CONDITION_NOT_FULFILLED,
                if (notExistTags.isNotEmpty()) {
                    "$notExistTags 태그가 존재하지 않습니다."
                } else {
                    "중복된 태그가 존재합니다."
                },
            )
        }
    }
}
