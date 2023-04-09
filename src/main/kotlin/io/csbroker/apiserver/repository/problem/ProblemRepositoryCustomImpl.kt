package io.csbroker.apiserver.repository.problem

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import io.csbroker.apiserver.dto.problem.GradingHistoryStats
import io.csbroker.apiserver.dto.problem.ProblemResponseDto
import io.csbroker.apiserver.dto.problem.ProblemSearchDto
import io.csbroker.apiserver.model.GradingHistory
import io.csbroker.apiserver.model.Problem
import io.csbroker.apiserver.model.QGradingHistory.gradingHistory
import io.csbroker.apiserver.model.QProblem.problem
import io.csbroker.apiserver.model.QProblemTag.problemTag
import io.csbroker.apiserver.model.QTag.tag
import io.csbroker.apiserver.model.QUser.user
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.util.Random

class ProblemRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory,
) : ProblemRepositoryCustom {

    override fun findProblemsByQuery(problemSearchDto: ProblemSearchDto): Page<ProblemResponseDto> {
        val totalProblemIds = getTotalProblemIdsWithFiltering(problemSearchDto)
        val ids = getPaginatedIds(totalProblemIds, problemSearchDto.pageable)
        val problems = getProblemsWithFetchJoin(ids)
        val gradingHistories = getGradingHistoriesRelatedProblems(problems)
        val stats = getStats(gradingHistories)

        return PageImpl(
            problems.map {
                it.toProblemResponseDto(stats[it.id])
            },
            problemSearchDto.pageable,
            totalProblemIds.size.toLong(),
        )
    }

    private fun getTotalProblemIdsWithFiltering(problemSearchDto: ProblemSearchDto): List<Long> {
        val ids = queryFactory.select(problem.id)
            .from(problem)
            .distinct()
            .leftJoin(problem.gradingHistory, gradingHistory)
            .leftJoin(gradingHistory.user, user)
            .leftJoin(problem.problemTags, problemTag)
            .leftJoin(problemTag.tag, tag)
            .where(
                likeTitle(problemSearchDto.query),
                inTags(problemSearchDto.tags),
                solvedBy(problemSearchDto.solvedBy, problemSearchDto.isSolved),
                isType(problemSearchDto.type),
                isGradable(problemSearchDto.isGradable),
                problem.isActive.isTrue,
            )
            .fetch()

        if (problemSearchDto.shuffle!!) {
            return ids.shuffled(Random(problemSearchDto.seed!!))
        }
        return ids.sortedDescending()
    }

    private fun getStats(gradingHistories: List<GradingHistory>) =
        gradingHistories.groupBy {
            it.problem.id
        }.map {
            it.key to GradingHistoryStats.toGradingHistoryStats(it.value)
        }.toMap()

    private fun getGradingHistoriesRelatedProblems(result: List<Problem>): List<GradingHistory> =
        queryFactory.selectFrom(gradingHistory)
            .distinct()
            .where(gradingHistory.problem.id.`in`(result.map { it.id }))
            .fetch()

    private fun getProblemsWithFetchJoin(ids: List<Long>): List<Problem> =
        queryFactory.selectFrom(problem)
            .distinct()
            .leftJoin(problem.gradingHistory, gradingHistory).fetchJoin()
            .leftJoin(gradingHistory.user, user).fetchJoin()
            .leftJoin(problem.problemTags, problemTag).fetchJoin()
            .leftJoin(problemTag.tag, tag).fetchJoin()
            .where(problem.id.`in`(ids))
            .orderBy(problem.createdAt.desc())
            .fetch()

    private fun getPaginatedIds(ids: List<Long>, pageable: Pageable): List<Long> {
        val start = pageable.offset.toInt()
        val end = (pageable.offset + pageable.pageSize).toInt()
        return ids.slice(start until end)
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

        val solvedProblemIds = queryFactory.select(gradingHistory.problem.id)
            .from(gradingHistory)
            .innerJoin(gradingHistory.user, user)
            .innerJoin(gradingHistory.problem, problem)
            .where(gradingHistory.user.email.eq(email), gradingHistory.score.eq(problem.score))

        return if (isSolved) problem.id.`in`(solvedProblemIds) else problem.id.notIn(solvedProblemIds)
    }
}