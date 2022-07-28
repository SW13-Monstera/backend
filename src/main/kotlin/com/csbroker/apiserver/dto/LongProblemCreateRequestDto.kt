package com.csbroker.apiserver.dto

import com.csbroker.apiserver.common.enums.GradingStandardType
import com.csbroker.apiserver.model.GradingStandard
import com.csbroker.apiserver.model.LongProblem
import com.csbroker.apiserver.model.User

data class LongProblemCreateRequestDto(
    val title: String,
    val description: String,
    val standardAnswer: String,
    val gradingStandards: List<GradingStandardData>
) {

    data class GradingStandardData(
        val content: String,
        val score: Double,
        val type: GradingStandardType,
    )

    fun toLongProblem(creator: User): LongProblem {
        return LongProblem(
            title = title,
            description = description,
            standardAnswer = standardAnswer,
            creator = creator
        )
    }

    fun getGradingStandardList(longProblem: LongProblem): List<GradingStandard> {
        return this.gradingStandards.map {
            GradingStandard(
                content = it.content,
                score = it.score,
                type = it.type,
                problem = longProblem
            )
        }
    }
}
