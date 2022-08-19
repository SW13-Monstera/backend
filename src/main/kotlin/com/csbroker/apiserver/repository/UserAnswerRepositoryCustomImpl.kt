package com.csbroker.apiserver.repository

import com.csbroker.apiserver.dto.useranswer.UserAnswerUpsertDto
import org.springframework.jdbc.core.BatchPreparedStatementSetter
import org.springframework.jdbc.core.JdbcTemplate
import java.nio.ByteBuffer
import java.sql.PreparedStatement
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.UUID

class UserAnswerRepositoryCustomImpl(
    private val jdbcTemplate: JdbcTemplate
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
            }
        )
    }

    private fun uuidAsByte(uuid: UUID?): ByteArray? {
        if (uuid == null) {
            return null
        }
        val byteBufferWrapper = ByteBuffer.wrap(ByteArray(16))
        byteBufferWrapper.putLong(uuid.mostSignificantBits)
        byteBufferWrapper.putLong(uuid.leastSignificantBits)
        return byteBufferWrapper.array()
    }
}
