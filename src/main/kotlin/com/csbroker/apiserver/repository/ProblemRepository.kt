package com.csbroker.apiserver.repository

import com.csbroker.apiserver.model.Problem
import org.springframework.data.jpa.repository.JpaRepository

interface ProblemRepository : JpaRepository<Problem, Long>, ProblemRepositoryCustom {
    fun deleteProblemsByIdIn(ids: List<Long>)
}
