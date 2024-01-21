package io.csbroker.apiserver.dto.problem.longproblem

import io.csbroker.apiserver.model.LongProblem
import io.csbroker.apiserver.model.User

data class LongProblemUpsertRequestDto(
    val title: String,
    val description: String,
    val standardAnswers: List<String>,
    val tags: List<String>,
    val isActive: Boolean = true,
) {

    fun toLongProblem(creator: User): LongProblem {
        return LongProblem(
            title = title,
            description = description,
            creator = creator,
        )
    }
}
