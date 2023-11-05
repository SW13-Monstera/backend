package io.csbroker.apiserver.repository.post

import io.csbroker.apiserver.model.Like
import io.csbroker.apiserver.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface LikeRepository : JpaRepository<Like, Long> {

    @Query(
        """
            select l from Like l
            where l.type = 'POST' and l.targetId in :postIds
        """,
    )
    fun findAllByPostIdIn(postIds: List<Long>): List<Like>

    @Query(
        """
            select l from Like l
            where l.type = 'COMMENT' and l.targetId in :commentIds
        """,
    )
    fun findByCommentIdIn(commentIds: List<Long>): List<Like>

    @Query(
        """
            select l from Like l
            where l.type = 'POST' and l.targetId = :postId and l.user = :user
        """,
    )
    fun findByPostIdAndUser(postId: Long, user: User): Like?

    @Query(
        """
            select l from Like l
            where l.type = 'COMMENT' and l.targetId = :commentId and l.user = :user
        """,
    )
    fun findByCommentIdAndUser(commentId: Long, user: User): Like?
}
