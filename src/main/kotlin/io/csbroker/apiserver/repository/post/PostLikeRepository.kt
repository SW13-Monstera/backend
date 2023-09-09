package io.csbroker.apiserver.repository.post

import io.csbroker.apiserver.model.Post
import io.csbroker.apiserver.model.PostLike
import io.csbroker.apiserver.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PostLikeRepository : JpaRepository<PostLike, Long> {
    fun findByPostAndUser(post: Post, user: User): PostLike?

    @Query(
        """
        select pl from PostLike pl
        join fetch pl.post
        join fetch pl.user
        where pl.post in :posts
    """,
    )
    fun findAllByPostIn(posts: List<Post>): List<PostLike>
}
