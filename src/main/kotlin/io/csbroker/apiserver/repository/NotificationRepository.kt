package io.csbroker.apiserver.repository

import io.csbroker.apiserver.model.Notification
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface NotificationRepository : JpaRepository<Notification, Long>, NotificationRepositoryCustom {
    fun findByUserId(userId: UUID, pageable: Pageable): Page<Notification>

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = TRUE WHERE n.id IN :notificationIds AND n.user.id = :userId")
    fun setIsReadByIdIn(@Param("userId") userId: UUID, @Param("notificationIds") notificationIds: List<Long>)

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = TRUE WHERE n.id = :id AND n.user.id = :userId")
    fun setIsReadById(@Param("userId") userId: UUID, @Param("id") id: Long)

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.isRead = FALSE")
    fun countUnReadByUserId(@Param("userId") userId: UUID): Long
}
