package io.csbroker.apiserver.model

import io.csbroker.apiserver.dto.problem.longproblem.LongProblemDetailResponseDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemResponseDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemSearchResponseDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemUpsertRequestDto
import io.csbroker.apiserver.dto.user.GradingStandardResponseDto
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "long_problem")
@DiscriminatorValue("long")
class LongProblem(
    title: String,
    description: String,
    creator: User,

    @Column(name = "standard_answer", columnDefinition = "VARCHAR(300)")
    var standardAnswer: String,

    @OneToMany(mappedBy = "problem", cascade = [CascadeType.ALL])
    var gradingStandards: MutableList<GradingStandard> = mutableListOf(),

    @OneToMany(mappedBy = "problem", cascade = [CascadeType.ALL])
    var userAnswers: MutableList<UserAnswer> = mutableListOf()
) : Problem(
    title = title,
    description = description,
    creator = creator,
    dtype = "long",
    isGradable = false,
    score = gradingStandards.sumOf { it.score }
) {
    fun addGradingStandards(gradingStandards: List<GradingStandard>) {
        this.gradingStandards.addAll(gradingStandards)
        this.score = gradingStandards.sumOf { it.score }
    }

    fun updateFromDto(upsertRequestDto: LongProblemUpsertRequestDto) {
        this.title = upsertRequestDto.title
        this.description = upsertRequestDto.description
        this.standardAnswer = upsertRequestDto.standardAnswer
        this.isGradable = upsertRequestDto.isGradable
        this.isActive = upsertRequestDto.isActive
    }

    fun toLongProblemResponseDto(): LongProblemResponseDto {
        return LongProblemResponseDto(
            this.id!!,
            this.title,
            this.description,
            this.standardAnswer,
            this.problemTags.map { it.tag.name },
            this.gradingStandards.map { GradingStandardResponseDto.fromGradingStandard(it) },
            this.isActive,
            this.isGradable
        )
    }

    fun toLongProblemDataDto(): LongProblemSearchResponseDto.LongProblemDataDto {
        val keywordScores = this.userAnswers.map {
            it.getKeywordScore()
        }

        val contentScores = this.userAnswers.map {
            it.getContentScore()
        }

        return LongProblemSearchResponseDto.LongProblemDataDto(
            this.id!!,
            this.title,
            this.creator.username,
            if (keywordScores.isEmpty()) null else keywordScores.average(),
            if (contentScores.isEmpty()) null else contentScores.average(),
            this.userAnswers.size,
            this.isActive
        )
    }

    fun toDetailResponseDto(email: String?): LongProblemDetailResponseDto {
        val tags = this.problemTags.map {
            it.tag
        }.map {
            it.name
        }

        val scoreList = this.gradingHistory.map {
            it.score
        }.toList().sorted()

        val isSolved = this.gradingHistory.any { it.user.email == email }

        return LongProblemDetailResponseDto(
            this.id!!,
            this.title,
            tags,
            this.description,
            if (scoreList.isEmpty()) null else scoreList.average(),
            if (scoreList.isEmpty()) null else scoreList.last(),
            if (scoreList.isEmpty()) null else scoreList.first(),
            scoreList.size,
            isSolved
        )
    }
}