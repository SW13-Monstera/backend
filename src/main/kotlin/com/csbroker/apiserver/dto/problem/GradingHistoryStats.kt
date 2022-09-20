package com.csbroker.apiserver.dto.problem

import com.csbroker.apiserver.model.GradingHistory

data class GradingHistoryStats(
    val avgScore: Double?,
    val totalSolved: Int
) {
    companion object {
        fun toGradingHistoryStats(gradingHistories: List<GradingHistory>): GradingHistoryStats {
            return GradingHistoryStats(
                gradingHistories.map {
                    it.score
                }.average().let {
                    if (it.isNaN()) {
                        null
                    } else {
                        it
                    }
                },
                gradingHistories.map {
                    it.user.username
                }.distinct().size
            )
        }
    }
}
