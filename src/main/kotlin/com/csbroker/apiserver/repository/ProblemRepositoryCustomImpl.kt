package com.csbroker.apiserver.repository

import com.csbroker.apiserver.dto.ProblemSearchDto
import com.csbroker.apiserver.model.Problem
import com.csbroker.apiserver.model.QGradingHistory.gradingHistory
import com.csbroker.apiserver.model.QProblem.problem
import com.csbroker.apiserver.model.QProblemTag.problemTag
import com.csbroker.apiserver.model.QTag.tag
import com.csbroker.apiserver.model.QUser.user
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Pageable

class ProblemRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory
) : ProblemRepositoryCustom {

    override fun findProblemsByQuery(problemSearchDto: ProblemSearchDto, pageable: Pageable): List<Problem> {
        return queryFactory.selectFrom(problem)
            .distinct()
            .leftJoin(problem.gradingHistory, gradingHistory)
            .leftJoin(gradingHistory.user, user)
            .leftJoin(problem.problemTags, problemTag)
            .leftJoin(problemTag.tag, tag)
            .where(
                this.likeTitle(problemSearchDto.query),
                this.inTags(problemSearchDto.tags),
                this.solvedBy(problemSearchDto.solvedBy)
            )
            .orderBy(problem.updatedAt.desc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()
    }

    private fun likeTitle(title: String): BooleanExpression? {
        return if (title.isBlank()) null else problem.title.containsIgnoreCase(title)
    }

    private fun inTags(tags: List<String>): BooleanExpression? {
        if (tags.isEmpty()) {
            return null
        }

        return tag.name.`in`(tags)
    }

    private fun solvedBy(username: String?): BooleanExpression? {
        if (username == null) {
            return null
        }

        return user.username.eq(username)
    }
}
