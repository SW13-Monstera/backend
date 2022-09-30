package com.csbroker.apiserver.repository

import com.csbroker.apiserver.dto.problem.GradingHistoryStats
import com.csbroker.apiserver.dto.problem.ProblemResponseDto
import com.csbroker.apiserver.dto.problem.ProblemSearchDto
import com.csbroker.apiserver.model.QGradingHistory.gradingHistory
import com.csbroker.apiserver.model.QProblem.problem
import com.csbroker.apiserver.model.QProblemTag.problemTag
import com.csbroker.apiserver.model.QTag.tag
import com.csbroker.apiserver.model.QUser.user
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

class ProblemRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory
) : ProblemRepositoryCustom {

    override fun findProblemsByQuery(
        problemSearchDto: ProblemSearchDto,
        pageable: Pageable
    ): Page<ProblemResponseDto> {
        val ids = this.queryFactory.select(problem.id)
            .from(problem)
            .distinct()
            .leftJoin(problem.gradingHistory, gradingHistory)
            .leftJoin(gradingHistory.user, user)
            .leftJoin(problem.problemTags, problemTag)
            .leftJoin(problemTag.tag, tag)
            .where(
                this.likeTitle(problemSearchDto.query),
                this.inTags(problemSearchDto.tags),
                this.solvedBy(problemSearchDto.solvedBy, problemSearchDto.isSolved),
                this.isType(problemSearchDto.type),
                this.isGradable(problemSearchDto.isGradable),
                problem.isActive.isTrue
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val result = this.queryFactory.selectFrom(problem)
            .distinct()
            .leftJoin(problem.gradingHistory, gradingHistory).fetchJoin()
            .leftJoin(gradingHistory.user, user).fetchJoin()
            .leftJoin(problem.problemTags, problemTag).fetchJoin()
            .leftJoin(problemTag.tag, tag).fetchJoin()
            .where(problem.id.`in`(ids))
            .fetch()

        val gradingHistories = this.queryFactory.selectFrom(gradingHistory)
            .distinct()
            .where(gradingHistory.problem.id.`in`(result.map { it.id }))
            .fetch()

        val stats = gradingHistories.groupBy {
            it.problem.id
        }.map {
            it.key to GradingHistoryStats.toGradingHistoryStats(it.value)
        }.toMap()

        val totalCnt = this.queryFactory.select(problem.id.count())
            .from(problem)
            .leftJoin(problem.gradingHistory, gradingHistory)
            .leftJoin(gradingHistory.user, user)
            .leftJoin(problem.problemTags, problemTag)
            .leftJoin(problemTag.tag, tag)
            .groupBy(problem.id)
            .where(
                this.likeTitle(problemSearchDto.query),
                this.inTags(problemSearchDto.tags),
                this.solvedBy(problemSearchDto.solvedBy, problemSearchDto.isSolved),
                this.isType(problemSearchDto.type),
                this.isGradable(problemSearchDto.isGradable),
                problem.isActive.isTrue
            )
            .fetch().size.toLong()

        return PageImpl(result.map { it.toProblemResponseDto(stats[it.id]) }, pageable, totalCnt)
    }

    private fun isGradable(isGradable: Boolean?): BooleanExpression? {
        return if (isGradable == null) null else problem.isGradable.eq(isGradable)
    }

    private fun isType(type: List<String>?): BooleanExpression? {
        return if (type.isNullOrEmpty()) null else problem.dtype.`in`(type)
    }

    private fun likeTitle(title: String?): BooleanExpression? {
        return if (title.isNullOrBlank()) null else problem.title.containsIgnoreCase(title)
    }

    private fun inTags(tags: List<String>?): BooleanExpression? {
        if (tags.isNullOrEmpty()) {
            return null
        }

        return tag.name.`in`(tags)
    }

    private fun solvedBy(email: String?, isSolved: Boolean?): BooleanExpression? {
        if (email == null || isSolved == null) {
            return null
        }

        return if (isSolved) user.email.eq(email) else user.email.ne(email)
    }
}
