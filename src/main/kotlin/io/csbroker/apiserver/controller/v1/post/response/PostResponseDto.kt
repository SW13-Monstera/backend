package io.csbroker.apiserver.controller.v1.post.response

import io.csbroker.apiserver.model.Comment
import io.csbroker.apiserver.model.Post
import java.time.LocalDateTime

data class PostResponseDto(
    val id: Long,
    val content: String,
    val username: String,
    val likeCount: Long,
    val isLiked: Boolean,
    val comments: List<CommentResponseDto>,
) {
    constructor(post: Post, likeCount: Long, isLiked: Boolean, comments: List<CommentResponseDto>) : this(
        id = post.id,
        content = post.content,
        username = post.user.username,
        likeCount = likeCount,
        isLiked = isLiked,
        comments = comments,
    )
}

data class CommentResponseDto(
    val id: Long,
    val content: String,
    val username: String,
    val createdAt: LocalDateTime,
) {
    constructor(comment: Comment) : this(
        id = comment.id,
        content = comment.content,
        username = comment.user.username,
        createdAt = comment.createdAt!!,
    )
}
