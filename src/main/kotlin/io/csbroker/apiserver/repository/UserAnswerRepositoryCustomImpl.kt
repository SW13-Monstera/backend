package io.csbroker.apiserver.repository

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import io.csbroker.apiserver.common.util.uuidAsByte
import io.csbroker.apiserver.dto.useranswer.UserAnswerUpsertDto
import io.csbroker.apiserver.model.QLongProblem.longProblem
import io.csbroker.apiserver.model.QUser.user
import io.csbroker.apiserver.model.QUserAnswer.userAnswer
import io.csbroker.apiserver.model.UserAnswer
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.jdbc.core.BatchPreparedStatementSetter
import org.springframework.jdbc.core.JdbcTemplate
import java.sql.PreparedStatement
import java.sql.Timestamp
import java.time.LocalDateTime

class UserAnswerRepositoryCustomImpl(
    private val jdbcTemplate: JdbcTemplate,
    private val queryFactory: JPAQueryFactory,
) : UserAnswerRepositoryCustom {
    override fun batchInsert(userAnswers: List<UserAnswerUpsertDto>) {
        val sql = """
            INSERT INTO user_answer
            (answer, is_labeled, is_validated, assigned_user_id, problem_id, validator_id, created_at, updated_at)
            VALUES
            (?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        jdbcTemplate.batchUpdate(
            sql,
            object : BatchPreparedStatementSetter {
                override fun setValues(ps: PreparedStatement, i: Int) {
                    val userAnswer = userAnswers[i]
                    ps.setString(1, userAnswer.answer)
                    ps.setBoolean(2, false)
                    ps.setBoolean(3, false)
                    ps.setBytes(4, uuidAsByte(userAnswer.assignedUserId))
                    ps.setLong(5, userAnswer.problemId)
                    ps.setBytes(6, uuidAsByte(userAnswer.validatingUserId))
                    ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()))
                    ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()))
                }

                override fun getBatchSize(): Int {
                    return userAnswers.size
                }
            },
        )
    }

    override fun findUserAnswersByQuery(
        id: Long?,
        assignedBy: String?,
        validatedBy: String?,
        problemTitle: String?,
        answer: String?,
        isLabeled: Boolean?,
        isValidated: Boolean?,
        pageable: Pageable,
    ): Page<UserAnswer> {
        val result = queryFactory.selectFrom(userAnswer)
            .distinct()
            .leftJoin(userAnswer.assignedUser, user).fetchJoin()
            .leftJoin(userAnswer.validatingUser, user).fetchJoin()
            .leftJoin(userAnswer.problem, longProblem).fetchJoin()
            .where(
                findById(id),
                isAssignedBy(assignedBy),
                isValidatedBy(validatedBy),
                likeProblemTitle(problemTitle),
                likeAnswer(answer),
                userAnswerLabeled(isLabeled),
                userAnswerValidated(isValidated),
            )
            .orderBy(userAnswer.updatedAt.desc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val totalCnt = queryFactory.selectFrom(userAnswer)
            .distinct()
            .leftJoin(userAnswer.assignedUser, user)
            .leftJoin(userAnswer.validatingUser, user)
            .leftJoin(userAnswer.problem, longProblem)
            .groupBy(userAnswer.id)
            .where(
                findById(id),
                isAssignedBy(assignedBy),
                isValidatedBy(validatedBy),
                likeProblemTitle(problemTitle),
                likeAnswer(answer),
                userAnswerLabeled(isLabeled),
                userAnswerValidated(isValidated),
            )
            .fetch().size.toLong()

        return PageImpl(result, pageable, totalCnt)
    }

    private fun findById(id: Long?): BooleanExpression? {
        return if (id == null) null else userAnswer.id.eq(id)
    }

    private fun isAssignedBy(assignedBy: String?): BooleanExpression? {
        return if (assignedBy == null) null else userAnswer.assignedUser.username.eq(assignedBy)
    }

    private fun isValidatedBy(validatedBy: String?): BooleanExpression? {
        return if (validatedBy == null) null else userAnswer.validatingUser.username.eq(validatedBy)
    }

    private fun likeProblemTitle(problemTitle: String?): BooleanExpression? {
        return if (problemTitle == null) null else longProblem.title.contains(problemTitle)
    }

    private fun likeAnswer(answer: String?): BooleanExpression? {
        return if (answer == null) null else userAnswer.answer.contains(answer)
    }

    private fun userAnswerLabeled(isLabeled: Boolean?): BooleanExpression? {
        return if (isLabeled == null) null else userAnswer.isLabeled.eq(isLabeled)
    }

    private fun userAnswerValidated(isValidated: Boolean?): BooleanExpression? {
        return if (isValidated == null) null else userAnswer.isValidated.eq(isValidated)
    }
}
