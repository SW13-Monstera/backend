package io.csbroker.apiserver.repository

import io.csbroker.apiserver.model.ProblemSet
import org.springframework.data.jpa.repository.JpaRepository

interface ProblemSetRepository : JpaRepository<ProblemSet, Long>
