package com.csbroker.apiserver.model

import com.csbroker.apiserver.dto.problem.MultipleChoiceProblemDetailResponseDto
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
) : Problem(title = title, description = description, creator = creator, dtype = "multiple") {
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
        this.isGradable = upsertRequestDto.isGradable
        this.isActive = upsertRequestDto.isActive
        this.score = upsertRequestDto.score
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
            this.score,
            this.isActive,
            this.isGradable
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

    fun toDetailResponseDto(): MultipleChoiceProblemDetailResponseDto {
        val tags = this.problemTags.map {
            it.tag
        }.map {
            it.name
        }

        val scoreList = this.gradingHistory.map {
            it.score
        }.toList().sorted()

        val totalSolved = this.gradingHistory.map {
            it.user.username
        }.distinct().size

        return MultipleChoiceProblemDetailResponseDto(
            this.id!!,
            this.title,
            tags,
            this.description,
            if (scoreList.isEmpty()) null else scoreList.average(),
            if (scoreList.isEmpty()) null else scoreList.first(),
            if (scoreList.isEmpty()) null else scoreList.last(),
            totalSolved,
            this.choicesList.map { it.toChoiceResponseDto() }
        )
    }
}
