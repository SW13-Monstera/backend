package com.csbroker.apiserver.dto

import com.csbroker.apiserver.model.ShortProblem
import com.csbroker.apiserver.model.User

data class ShortProblemCreateRequestDto(
    val title: String,
    val description: String,
    val answer: String
) {
    fun toShortProblem(creator: User): ShortProblem {
        return ShortProblem(
            title = title,
            description = description,
            creator = creator,
            answer = answer
        )
    }
}
