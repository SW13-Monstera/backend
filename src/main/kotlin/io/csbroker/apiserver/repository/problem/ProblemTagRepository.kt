package io.csbroker.apiserver.repository.problem

import io.csbroker.apiserver.model.ProblemTag
import org.springframework.data.jpa.repository.JpaRepository

interface ProblemTagRepository : JpaRepository<ProblemTag, Long>
