package io.csbroker.apiserver.model

import io.csbroker.apiserver.dto.notification.NotificationRequestDto
import io.csbroker.apiserver.dto.notification.NotificationResponseDto
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.Where

@Entity
@Table(name = "notification")
@SQLDelete(sql = "UPDATE notification SET is_deleted = true WHERE notification_id = ?")
@Where(clause = "is_deleted = false")
class Notification(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    val id: Long = 0,

    @Column(name = "content", columnDefinition = "VARCHAR(100)")
    val content: String,

    @Column(name = "link", columnDefinition = "VARCHAR(300)")
    val link: String,

    @Column(name = "is_read")
    val isRead: Boolean = false,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User,

    @Column(name = "is_deleted")
    var isDeleted: Boolean = false,
) : BaseEntity() {
    constructor(notificationRequestDto: NotificationRequestDto, user: User) : this(
        content = notificationRequestDto.content,
        link = notificationRequestDto.link,
        user = user,
    )

    fun toNotificationResponseDto(): NotificationResponseDto {
        return NotificationResponseDto(
            id,
            content,
            link,
            isRead,
            createdAt!!,
        )
    }
}
