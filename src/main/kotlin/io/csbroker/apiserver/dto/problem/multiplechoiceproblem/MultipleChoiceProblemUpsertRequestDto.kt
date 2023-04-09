package io.csbroker.apiserver.dto.problem.multiplechoiceproblem

import io.csbroker.apiserver.model.Choice
import io.csbroker.apiserver.model.MultipleChoiceProblem
import io.csbroker.apiserver.model.User

data class MultipleChoiceProblemUpsertRequestDto(
    val title: String,
    val description: String,
    val tags: MutableList<String>,
    val choices: MutableList<ChoiceData>,
    val score: Double,
    val isGradable: Boolean = true,
    val isActive: Boolean = true,
) {
    data class ChoiceData(
        val content: String,
        val isAnswer: Boolean,
    )

    fun toMultipleChoiceProblem(creator: User): MultipleChoiceProblem {
        val isMultiple = choices.any { it.isAnswer }

        return MultipleChoiceProblem(
            title = title,
            description = description,
            creator = creator,
            isMultiple = isMultiple,
            score = score,
        )
    }

    fun getChoiceList(multipleChoiceProblem: MultipleChoiceProblem): List<Choice> {
        return choices.map {
            Choice(
                content = it.content,
                isAnswer = it.isAnswer,
                multipleChoiceProblem = multipleChoiceProblem,
            )
        }
    }
}
