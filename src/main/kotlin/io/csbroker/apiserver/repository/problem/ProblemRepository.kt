package io.csbroker.apiserver.repository.problem

import io.csbroker.apiserver.model.Problem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ProblemRepository : JpaRepository<Problem, Long>, ProblemRepositoryCustom {
    fun deleteProblemsByIdIn(ids: List<Long>)

    @Query("select count(p) from Problem p where p.isGradable = TRUE and p.isActive = TRUE")
    fun countGradableProblem(): Long

    @Query("select problem_id from problem p order by RAND() limit :size", nativeQuery = true)
    fun findRandomProblemIds(@Param(value = "size") size: Int): List<Long>

}
