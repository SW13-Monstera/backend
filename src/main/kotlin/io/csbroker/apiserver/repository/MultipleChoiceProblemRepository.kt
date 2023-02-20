package io.csbroker.apiserver.repository

import io.csbroker.apiserver.model.MultipleChoiceProblem
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface MultipleChoiceProblemRepository : JpaRepository<MultipleChoiceProblem, Long> {
    @Query(
        "SELECT mp FROM MultipleChoiceProblem mp WHERE (mp.id = :id OR :id IS NULL)" +
            "AND (mp.title LIKE '%'||:title||'%' OR :title IS NULL) " +
            "AND (mp.description LIKE '%'||:description||'%' OR :description IS NULL)",
    )
    fun findMultipleChoiceProblemsByQuery(
        @Param("id") id: Long?,
        @Param("title") title: String?,
        @Param("description") description: String?,
        pageable: Pageable,
    ): Page<MultipleChoiceProblem>
}
