package io.csbroker.apiserver.model

import io.csbroker.apiserver.dto.problem.ProblemCommonDetailResponse
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemDetailResponseDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemResponseDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemSearchResponseDto
import io.csbroker.apiserver.dto.problem.multiplechoiceproblem.MultipleChoiceProblemUpsertRequestDto
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
        choicesList.add(choice)
        choice.multipleChoiceProblem = this
    }

    fun addChoices(choices: List<Choice>) {
        choicesList.addAll(choices)
        choices.forEach {
            it.multipleChoiceProblem = this
        }
    }

    fun updateFromDto(upsertRequestDto: MultipleChoiceProblemUpsertRequestDto) {
        title = upsertRequestDto.title
        description = upsertRequestDto.description
        isGradable = upsertRequestDto.isGradable
        isActive = upsertRequestDto.isActive
        score = upsertRequestDto.score
    }

    fun toMultipleChoiceProblemResponseDto(): MultipleChoiceProblemResponseDto {
        return MultipleChoiceProblemResponseDto(
            id!!,
            title,
            description,
            problemTags.map {
                it.tag.name
            },
            isMultiple,
            choicesList.map {
                MultipleChoiceProblemUpsertRequestDto.ChoiceData(
                    it.content,
                    it.isAnswer
                )
            },
            score,
            isActive,
            isGradable
        )
    }

    fun toMultipleChoiceDataDto(): MultipleChoiceProblemSearchResponseDto.MultipleChoiceProblemDataDto {
        val answerCnt = gradingHistory.size
        val correctAnswerCnt = gradingHistory.count {
            it.score == score
        }

        return MultipleChoiceProblemSearchResponseDto.MultipleChoiceProblemDataDto(
            id!!,
            title,
            creator.username,
            if (answerCnt == 0) null else correctAnswerCnt / answerCnt.toDouble(),
            answerCnt,
            isActive
        )
    }

    fun toDetailResponseDto(email: String?): MultipleChoiceProblemDetailResponseDto {
        val commonDetail = ProblemCommonDetailResponse.getCommonDetail(this)

        return MultipleChoiceProblemDetailResponseDto(
            id!!,
            title,
            commonDetail.tags,
            description,
            commonDetail.correctSubmission,
            commonDetail.correctUserCnt,
            commonDetail.totalSubmission,
            choicesList.map { it.toChoiceResponseDto() },
            gradingHistory.any { it.user.email == email },
            isMultiple
        )
    }
}
