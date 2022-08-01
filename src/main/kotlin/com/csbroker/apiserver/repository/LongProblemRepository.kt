package com.csbroker.apiserver.repository

import com.csbroker.apiserver.model.LongProblem
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface LongProblemRepository : JpaRepository<LongProblem, Long> {
    @Query(
        "SELECT lp FROM LongProblem lp WHERE (lp.id = :id OR :id IS NULL)" +
            "AND (lp.title LIKE '%'||:title||'%' OR :title IS NULL) " +
            "AND (lp.description LIKE '%'||:description||'%' OR :description IS NULL)"
    )
    fun findLongProblemsByQuery(
        @Param("id") id: Long?,
        @Param("title") title: String?,
        @Param("description") description: String?,
        pageable: Pageable
    ): Page<LongProblem>
}
