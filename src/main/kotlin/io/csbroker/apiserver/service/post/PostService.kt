package io.csbroker.apiserver.service.post

import io.csbroker.apiserver.controller.v1.post.response.PostResponseDto

interface PostService {
    fun findByProblemId(problemId: Long, email: String?): List<PostResponseDto>
    fun create(problemId: Long, content: String, email: String): Long
    fun like(id: Long, email: String)
    fun deleteById(id: Long, email: String)
}
