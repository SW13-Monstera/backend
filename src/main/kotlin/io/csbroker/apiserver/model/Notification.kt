package io.csbroker.apiserver.model

import io.csbroker.apiserver.dto.notification.NotificationRequestDto
import io.csbroker.apiserver.dto.notification.NotificationResponseDto
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "notification")
class Notification(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    val id: Long? = null,

    @Column(name = "content", columnDefinition = "VARCHAR(100)")
    val content: String,

    @Column(name = "link", columnDefinition = "VARCHAR(300)")
    val link: String,

    @Column(name = "is_read")
    val isRead: Boolean = false,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User
) : BaseEntity() {
    constructor(notificationRequestDto: NotificationRequestDto, user: User) : this(
        content = notificationRequestDto.content,
        link = notificationRequestDto.link,
        user = user
    )

    fun toNotificationResponseDto(): NotificationResponseDto {
        return NotificationResponseDto(
            this.id!!,
            this.content,
            this.link,
            this.isRead,
            this.createdAt!!
        )
    }
}
