package com.csbroker.apiserver.dto.problem

import com.csbroker.apiserver.model.Choice
import com.csbroker.apiserver.model.MultipleChoiceProblem
import com.csbroker.apiserver.model.User

data class MultipleChoiceProblemUpsertRequestDto(
    val title: String,
    val description: String,
    val tags: MutableList<String>,
    val choices: MutableList<ChoiceData>
) {
    data class ChoiceData(
        val content: String,
        val isAnswer: Boolean
    )

    fun toMultipleChoiceProblem(creator: User): MultipleChoiceProblem {
        val isMultiple = this.choices.count { it.isAnswer } > 1

        return MultipleChoiceProblem(
            title = title,
            description = description,
            creator = creator,
            isMultiple = isMultiple
        )
    }

    fun getChoiceList(multipleChoiceProblem: MultipleChoiceProblem): List<Choice> {
        return this.choices.map {
            Choice(
                content = it.content,
                isAnswer = it.isAnswer,
                multipleChoiceProblem = multipleChoiceProblem
            )
        }
    }
}