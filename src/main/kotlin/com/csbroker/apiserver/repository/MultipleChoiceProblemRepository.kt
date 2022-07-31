package com.csbroker.apiserver.repository

import com.csbroker.apiserver.model.MultipleChoiceProblem
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface MultipleChoiceProblemRepository : JpaRepository<MultipleChoiceProblem, Long> {
    @Query(
        "SELECT mp FROM MultipleChoiceProblem mp WHERE (:id is null or mp.id = :id) " +
            "and (:title is null or mp.title = :title) or (:description is null or mp.description = :description)"
    )
    fun findMultipleChoiceProblemsByQuery(
        @Param("id") id: Long?,
        @Param("title") title: String?,
        @Param("description") description: String?,
        pageable: Pageable
    ): Page<MultipleChoiceProblem>
}
