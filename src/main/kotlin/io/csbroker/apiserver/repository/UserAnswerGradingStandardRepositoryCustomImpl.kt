package io.csbroker.apiserver.repository

import org.springframework.jdbc.core.BatchPreparedStatementSetter
import org.springframework.jdbc.core.JdbcTemplate
import java.sql.PreparedStatement

class UserAnswerGradingStandardRepositoryCustomImpl(
    private val jdbcTemplate: JdbcTemplate
) : UserAnswerGradingStandardRepositoryCustom {
    override fun batchInsert(userAnswerId: Long, gradingStandardIds: List<Long>) {
        val sql = """
            INSERT INTO USER_ANSWER_GRADING_STANDARD
            (grading_standard_id, user_answer_id)
            VALUES
            (?, ?)
        """.trimIndent()

        jdbcTemplate.batchUpdate(
            sql,
            object : BatchPreparedStatementSetter {
                override fun setValues(ps: PreparedStatement, i: Int) {
                    val gradingStandardId = gradingStandardIds[i]
                    ps.setLong(1, gradingStandardId)
                    ps.setLong(2, userAnswerId)
                }

                override fun getBatchSize(): Int {
                    return gradingStandardIds.size
                }
            }
        )
    }
}
