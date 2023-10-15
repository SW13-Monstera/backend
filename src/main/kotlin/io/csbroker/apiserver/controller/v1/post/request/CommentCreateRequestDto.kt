package io.csbroker.apiserver.controller.v1.post.request

data class CommentCreateRequestDto(
    val postId: Long,
    val content: String,
)
