package io.csbroker.apiserver.controller.v1.post.response

import io.csbroker.apiserver.model.Post

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
