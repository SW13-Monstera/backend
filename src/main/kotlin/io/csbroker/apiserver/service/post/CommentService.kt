package io.csbroker.apiserver.service.post

import io.csbroker.apiserver.model.User

interface CommentService {
    fun create(postId: Long, content: String, user: User): Long
    fun deleteById(id: Long, user: User)
    fun like(id: Long, user: User)
}
