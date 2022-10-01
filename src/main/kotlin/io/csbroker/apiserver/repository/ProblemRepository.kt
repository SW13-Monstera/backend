package io.csbroker.apiserver.repository

import io.csbroker.apiserver.model.Problem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ProblemRepository : JpaRepository<Problem, Long>, ProblemRepositoryCustom {
    fun deleteProblemsByIdIn(ids: List<Long>)

    @Query("select count(p) from Problem p where p.isGradable = TRUE and p.isActive = TRUE")
    fun countGradableProblem(): Long
}
