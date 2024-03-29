package io.csbroker.apiserver.repository.post

import io.csbroker.apiserver.common.enums.LikeType
import io.csbroker.apiserver.model.Like
import io.csbroker.apiserver.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface LikeRepository : JpaRepository<Like, Long> {

    @Query(
        """
            select l from Like l
            join fetch l.user
            where l.type = :type and l.targetId in :targetIds
        """,
    )
    fun findAllByTargetIdIn(type: LikeType, targetIds: List<Long>): List<Like>

    @Query(
        """
            select l from Like l
            join fetch l.user
            where l.type = :type and l.targetId = :targetId
        """,
    )
    fun findAllByTargetId(type: LikeType, targetId: Long): List<Like>

    @Query(
        """
            select l from Like l
            join fetch l.user
            where l.type = :type and l.targetId = :targetId and l.user = :user
        """,
    )
    fun findByTargetIdAndUser(type: LikeType, targetId: Long, user: User): Like?
}
