package io.csbroker.apiserver.controller.v1.post.response

import java.time.LocalDateTime

data class PostResponseDto(
    val id: Long,
    val content: String,
    val username: String,
    val likeCount: Long,
    val isLiked: Boolean,
    val comments: List<CommentResponseDto>,
)

data class CommentResponseDto(
    val id: Long,
    val content: String,
    val username: String,
    val createdAt: LocalDateTime,
)
