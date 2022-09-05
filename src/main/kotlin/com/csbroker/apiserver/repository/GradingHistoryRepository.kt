package com.csbroker.apiserver.repository

import com.csbroker.apiserver.model.GradingHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface GradingHistoryRepository : JpaRepository<GradingHistory, Long> {
    @Query(
        "select gh from GradingHistory gh " +
            "join fetch gh.problem p " +
            "left join fetch p.problemTags pt " +
            "left join fetch pt.tag where gh.user.id = :id"
    )
    fun findGradingHistoriesByUserId(@Param("id") id: UUID): List<GradingHistory>
}
