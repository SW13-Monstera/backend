package io.csbroker.apiserver.controller.v1.post.response

import io.csbroker.apiserver.model.Post
import java.util.UUID

data class PostResponseDto(
    val id: Long,
    val content: String,
    val username: String,
    val userId: UUID,
    val likeCount: Long,
    val isLiked: Boolean,
    val comments: List<CommentResponseDto>,
) {
    constructor(post: Post, likeCount: Long, isLiked: Boolean, comments: List<CommentResponseDto>) : this(
        id = post.id,
        content = post.content,
        userId = post.user.id!!,
        username = post.user.username,
        likeCount = likeCount,
        isLiked = isLiked,
        comments = comments.sortedBy { it.createdAt },
    )
}
