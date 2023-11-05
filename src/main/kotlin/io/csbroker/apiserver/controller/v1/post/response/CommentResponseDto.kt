package io.csbroker.apiserver.controller.v1.post.response

import io.csbroker.apiserver.model.Comment
import java.time.LocalDateTime

data class CommentResponseDto(
    val id: Long,
    val content: String,
    val username: String,
    val likeCount: Int,
    val isLiked: Boolean,
    val createdAt: LocalDateTime,
) {
    constructor(comment: Comment, likeCount: Int, isLiked: Boolean) : this(
        id = comment.id,
        content = comment.content,
        username = comment.user.username,
        likeCount = likeCount,
        isLiked = isLiked,
        createdAt = comment.createdAt!!,
    )
}
