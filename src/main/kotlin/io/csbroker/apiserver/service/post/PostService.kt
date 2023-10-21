package io.csbroker.apiserver.service.post

import io.csbroker.apiserver.controller.v1.post.response.PostResponseDto
import io.csbroker.apiserver.model.User

interface PostService {
    fun findByProblemId(problemId: Long, email: String?): List<PostResponseDto>
    fun create(problemId: Long, content: String, user: User): Long
    fun like(id: Long, user: User)
    fun deleteById(id: Long, user: User)
}
