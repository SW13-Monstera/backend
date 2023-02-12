package io.csbroker.apiserver.repository

import io.csbroker.apiserver.model.ProblemSetMapping
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ProblemSetMappingRepository : JpaRepository<ProblemSetMapping, Long> {
    @Modifying
    @Query("DELETE ProblemSetMapping psm WHERE psm.problemSet.id = :problemSetId")
    fun deleteAllByProblemSetId(@Param("problemSetId") problemSetId: Long)
}
