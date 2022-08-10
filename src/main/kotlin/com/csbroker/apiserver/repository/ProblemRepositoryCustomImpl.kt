package com.csbroker.apiserver.repository

import com.csbroker.apiserver.dto.problem.ProblemSearchDto
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
            .leftJoin(problem.gradingHistory, gradingHistory).fetchJoin()
            .leftJoin(gradingHistory.user, user).fetchJoin()
            .leftJoin(problem.problemTags, problemTag).fetchJoin()
            .leftJoin(problemTag.tag, tag).fetchJoin()
            .groupBy()
            .where(
                this.likeTitle(problemSearchDto.query),
                this.inTags(problemSearchDto.tags),
                this.solvedBy(problemSearchDto.solvedBy),
                this.isType(problemSearchDto.type),
                this.isGradable(problemSearchDto.isGradable)
            )
            .orderBy(problem.updatedAt.desc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()
    }

    private fun isGradable(isGradable: Boolean?): BooleanExpression? {
        return if (isGradable == null) null else problem.isGradable.eq(isGradable)
    }

    private fun isType(type: String): BooleanExpression? {
        return if (type.isBlank()) null else problem.dtype.eq(type)
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

    private fun solvedBy(email: String?): BooleanExpression? {
        if (email == null) {
            return null
        }

        return user.email.eq(email)
    }
}
