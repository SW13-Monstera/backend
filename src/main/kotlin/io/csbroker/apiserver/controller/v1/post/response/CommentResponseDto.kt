package io.csbroker.apiserver.controller.v1.post.response

import io.csbroker.apiserver.model.Comment
import java.time.LocalDateTime
import java.util.UUID

data class CommentResponseDto(
    val id: Long,
    val content: String,
    val username: String,
    val userId: UUID,
    val likeCount: Long,
    val isLiked: Boolean,
    val createdAt: LocalDateTime,
) {
    constructor(comment: Comment, likeCount: Long, isLiked: Boolean) : this(
        id = comment.id,
        content = comment.content,
        userId = comment.user.id!!,
        username = comment.user.username,
        likeCount = likeCount,
        isLiked = isLiked,
        createdAt = comment.createdAt!!,
    )
}
