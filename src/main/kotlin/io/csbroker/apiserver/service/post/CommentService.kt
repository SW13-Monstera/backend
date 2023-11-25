package io.csbroker.apiserver.service.post

interface CommentService {
    fun create(postId: Long, content: String, email: String): Long
    fun deleteById(id: Long, email: String)
}
