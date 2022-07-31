package com.csbroker.apiserver.model

import com.csbroker.apiserver.dto.problem.MultipleChoiceProblemSearchResponseDto
import com.csbroker.apiserver.dto.problem.MultipleChoiceProblemUpsertRequestDto
import com.csbroker.apiserver.dto.problem.MultipleProblemResponseDto
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

    @Column(name = "score")
    var score: Double,

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

    fun toMultipleChoiceProblemResponseDto(): MultipleProblemResponseDto {
        return MultipleProblemResponseDto(
            this.id!!,
            this.title,
            this.description,
            this.problemTags.map {
                it.tag.name
            },
            this.isMultiple,
            this.choicesList.map {
                MultipleChoiceProblemUpsertRequestDto.ChoiceData(
                    it.content,
                    it.isAnswer
                )
            },
            this.score
        )
    }

    fun toMultipleChoiceDataDto(): MultipleChoiceProblemSearchResponseDto.MultipleChoiceProblemDataDto {
        val answerCnt = this.gradingHistory.size
        val correctAnswerCnt = this.gradingHistory.count {
            it.score == this.score
        }

        return MultipleChoiceProblemSearchResponseDto.MultipleChoiceProblemDataDto(
            this.id!!,
            this.title,
            this.creator.username,
            if (answerCnt == 0) null else correctAnswerCnt / answerCnt.toDouble(),
            answerCnt,
            this.isActive
        )
    }
}
