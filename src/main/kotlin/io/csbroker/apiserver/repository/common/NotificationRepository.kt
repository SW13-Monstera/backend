package io.csbroker.apiserver.repository.common

import io.csbroker.apiserver.model.Notification
import io.csbroker.apiserver.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface NotificationRepository : JpaRepository<Notification, Long> {
    fun findByUserId(userId: UUID, pageable: Pageable): Page<Notification>

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = TRUE WHERE n.id IN :notificationIds AND n.user.id = :userId")
    fun setIsReadByIdIn(@Param("userId") userId: UUID, @Param("notificationIds") notificationIds: List<Long>): Int

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = TRUE WHERE n.id = :id AND n.user.id = :userId")
    fun setIsReadById(@Param("userId") userId: UUID, @Param("id") id: Long): Int

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.isRead = FALSE")
    fun countUnReadByUserId(@Param("userId") userId: UUID): Long

    fun deleteAllByUserAndIdIn(user: User, id: List<Long>): Int
}
