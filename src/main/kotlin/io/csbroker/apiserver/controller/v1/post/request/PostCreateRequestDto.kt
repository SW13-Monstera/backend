package io.csbroker.apiserver.controller.v1.post.request

data class PostCreateRequestDto(
    val problemId: Long,
    val content: String,
)
