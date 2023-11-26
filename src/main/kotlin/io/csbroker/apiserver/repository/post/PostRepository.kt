package io.csbroker.apiserver.repository.post

import io.csbroker.apiserver.model.Post
import io.csbroker.apiserver.model.Problem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PostRepository : JpaRepository<Post, Long> {
    @Query(
        """
        select p from Post p
        join fetch p.user
        left join fetch p.comments
        where p.problem = :problem
    """,
    )
    fun findAllByProblem(problem: Problem): List<Post>

    @Query(
        """
            select p from Post p
            join fetch p.user
            left join fetch p.comments
            where p.id = :id
        """,
    )
    fun findByIdOrNull(id: Long): Post?
}
