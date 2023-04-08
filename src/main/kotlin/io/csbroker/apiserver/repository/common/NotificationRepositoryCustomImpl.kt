package io.csbroker.apiserver.repository.common

import io.csbroker.apiserver.common.util.uuidAsByte
import io.csbroker.apiserver.dto.notification.NotificationRequestDto
import org.springframework.jdbc.core.BatchPreparedStatementSetter
import org.springframework.jdbc.core.JdbcTemplate
import java.sql.PreparedStatement
import java.sql.Timestamp
import java.time.LocalDateTime

class NotificationRepositoryCustomImpl(
    private val jdbcTemplate: JdbcTemplate,
) : NotificationRepositoryCustom {
    override fun insertBulkNotifications(notifications: List<NotificationRequestDto>) {
        val sql = """
            INSERT INTO notification
            (content, link, is_read, user_id, created_at, updated_at)
            VALUES
            (?, ?, ?, ?, ?, ?)
        """.trimIndent()

        jdbcTemplate.batchUpdate(
            sql,
            object : BatchPreparedStatementSetter {
                override fun setValues(ps: PreparedStatement, i: Int) {
                    val notification = notifications[i]
                    ps.setString(1, notification.content)
                    ps.setString(2, notification.link)
                    ps.setBoolean(3, false)
                    ps.setBytes(4, uuidAsByte(notification.userId))
                    ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()))
                    ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()))
                }

                override fun getBatchSize(): Int {
                    return notifications.size
                }
            },
        )
    }
}
