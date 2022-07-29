package com.csbroker.apiserver.model

import com.csbroker.apiserver.dto.problem.MultipleChoiceProblemUpsertRequestDto
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "multiple_choice_problem")
@DiscriminatorValue("multiple")
class MultipleChoiceProblem(
    title: String,
    description: String,
    creator: User,
    @Column(name = "is_multiple")
    var isMultiple: Boolean,

    @OneToMany(mappedBy = "multipleChoiceProblem", cascade = [CascadeType.ALL])
    val choicesList: MutableList<Choice> = arrayListOf()
) : Problem(title = title, description = description, creator = creator) {
    fun addChoice(choice: Choice) {
        this.choicesList.add(choice)
        choice.multipleChoiceProblem = this
    }

    fun addChoices(choices: List<Choice>) {
        this.choicesList.addAll(choices)
        choices.forEach {
            it.multipleChoiceProblem = this
        }
    }

    fun updateFromDto(upsertRequestDto: MultipleChoiceProblemUpsertRequestDto) {
        this.title = upsertRequestDto.title
        this.description = upsertRequestDto.description
    }
}
