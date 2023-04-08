package io.csbroker.apiserver.repository.problem

import io.csbroker.apiserver.model.GradingStandard
import org.springframework.data.jpa.repository.JpaRepository

interface GradingStandardRepository : JpaRepository<GradingStandard, Long> {
    fun countByIdIn(ids: List<Long>): Int
}
