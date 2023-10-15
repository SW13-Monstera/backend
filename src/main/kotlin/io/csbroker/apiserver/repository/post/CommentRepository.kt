package io.csbroker.apiserver.repository.post

import io.csbroker.apiserver.model.Comment
import io.csbroker.apiserver.model.Post
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface CommentRepository : JpaRepository<Comment, Long> {
    @Query(
        """
        select c from Comment c
        join fetch c.post
        join fetch c.user
        where c.post in :posts
    """,
    )
    fun findAllByPostIn(posts: List<Post>): List<Comment>
}
