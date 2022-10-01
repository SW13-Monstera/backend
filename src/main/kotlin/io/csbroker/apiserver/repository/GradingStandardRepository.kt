package io.csbroker.apiserver.repository

import io.csbroker.apiserver.model.GradingStandard
import org.springframework.data.jpa.repository.JpaRepository

interface GradingStandardRepository : JpaRepository<GradingStandard, Long> {
    fun countByIdIn(ids: List<Long>): Int
}
