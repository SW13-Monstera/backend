package com.csbroker.apiserver.model

import com.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemDetailResponseDto
import com.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemResponseDto
import com.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemSearchResponseDto
import com.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemUpsertRequestDto
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
    score: Double,

    @Column(name = "is_multiple")
    var isMultiple: Boolean,

    @OneToMany(mappedBy = "multipleChoiceProblem", cascade = [CascadeType.ALL])
    val choicesList: MutableList<Choice> = arrayListOf()
) : Problem(title = title, description = description, creator = creator, dtype = "multiple", score = score) {
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

    fun toMultipleChoiceProblemResponseDto(): MultipleChoiceProblemResponseDto {
        return MultipleChoiceProblemResponseDto(
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

        var correctUserCnt = 0

        this.gradingHistory.groupBy {
            it.user.id
        }.forEach {
            if (it.value.any { gh -> gh.score == gh.problem.score }) {
                correctUserCnt++
            }
        }

        return MultipleChoiceProblemDetailResponseDto(
            this.id!!,
            this.title,
            tags,
            this.description,
            scoreList.count { it == this.score },
            correctUserCnt,
            scoreList.size,
            this.choicesList.map { it.toChoiceResponseDto() }
        )
    }
}
