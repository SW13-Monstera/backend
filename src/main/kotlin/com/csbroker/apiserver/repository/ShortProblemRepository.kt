package com.csbroker.apiserver.repository

import com.csbroker.apiserver.model.ShortProblem
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ShortProblemRepository : JpaRepository<ShortProblem, Long> {
    @Query(
        "SELECT sp FROM ShortProblem sp WHERE (:id is null or sp.id = :id) " +
            "and (:title is null or sp.title = :title) or (:description is null or sp.description = :description)"
    )
    fun findShortProblemsByQuery(
        @Param("id") id: Long?,
        @Param("title") title: String?,
        @Param("description") description: String?,
        pageable: Pageable
    ): Page<ShortProblem>
}
