package io.csbroker.apiserver.service.post

import io.csbroker.apiserver.controller.v1.post.response.PostResponseDto
import io.csbroker.apiserver.model.User

interface PostService {
    fun findByProblemId(problemId: Long, emailIfLogin: String?): List<PostResponseDto>
    fun findByPostId(postId: Long, emailIfLogin: String?): PostResponseDto
    fun create(problemId: Long, content: String, user: User): Long
    fun like(id: Long, user: User)
    fun deleteById(id: Long, user: User)
}
