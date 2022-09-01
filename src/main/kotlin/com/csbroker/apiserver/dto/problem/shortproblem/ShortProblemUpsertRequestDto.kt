package com.csbroker.apiserver.dto.problem.shortproblem

import com.csbroker.apiserver.model.ShortProblem
import com.csbroker.apiserver.model.User

data class ShortProblemUpsertRequestDto(
    val title: String,
    val description: String,
    val tags: MutableList<String>,
    val answer: String,
    val score: Double,
    val isGradable: Boolean = true,
    val isActive: Boolean = true
) {
    fun toShortProblem(creator: User): ShortProblem {
        return ShortProblem(
            title = title,
            description = description,
            creator = creator,
            answer = answer,
            score = score
        )
    }
}