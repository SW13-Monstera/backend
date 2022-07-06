package com.csbroker.apiserver.repository

import com.csbroker.apiserver.model.Problem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ProblemRepository : JpaRepository<Problem, UUID> {
    fun findByTitleContainingIgnoreCase(@Param("title") title: String): List<Problem>
}
