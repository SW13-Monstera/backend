package io.csbroker.apiserver.repository

import io.csbroker.apiserver.model.ShortProblem
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ShortProblemRepository : JpaRepository<ShortProblem, Long> {
    @Query(
        "SELECT sp FROM ShortProblem sp WHERE (sp.id = :id OR :id IS NULL)" +
            "AND (sp.title LIKE '%'||:title||'%' OR :title IS NULL) " +
            "AND (sp.description LIKE '%'||:description||'%' OR :description IS NULL)"
    )
    fun findShortProblemsByQuery(
        @Param("id") id: Long?,
        @Param("title") title: String?,
        @Param("description") description: String?,
        pageable: Pageable
    ): Page<ShortProblem>
}
