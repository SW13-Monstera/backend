package io.csbroker.apiserver.dto.problem.longproblem

import io.csbroker.apiserver.common.enums.GradingStandardType
import io.csbroker.apiserver.model.GradingStandard
import io.csbroker.apiserver.model.LongProblem
import io.csbroker.apiserver.model.User

data class LongProblemUpsertRequestDto(
    val title: String,
    val description: String,
    val standardAnswers: List<String>,
    val tags: MutableList<String>,
    val gradingStandards: MutableList<GradingStandardData>,
    val isGradable: Boolean = false,
    val isActive: Boolean = true,
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
            standardAnswer = "",
            creator = creator,
        )
    }

    fun getGradingStandardList(longProblem: LongProblem): List<GradingStandard> {
        return gradingStandards.map {
            GradingStandard(
                content = it.content,
                score = it.score,
                type = it.type,
                problem = longProblem,
            )
        }
    }
}
