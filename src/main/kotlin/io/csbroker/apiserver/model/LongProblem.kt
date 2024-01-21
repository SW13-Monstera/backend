package io.csbroker.apiserver.model

import io.csbroker.apiserver.dto.problem.longproblem.LongProblemDetailResponseDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemResponseDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemSearchResponseDto.LongProblemDataDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemUpsertRequestDto
import jakarta.persistence.CascadeType
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "long_problem")
@DiscriminatorValue("long")
class LongProblem(
    title: String,
    description: String,
    creator: User,

    @OneToMany(mappedBy = "problem", cascade = [CascadeType.ALL])
    var gradingStandards: MutableList<GradingStandard> = mutableListOf(),

    @OneToMany(mappedBy = "problem", cascade = [CascadeType.ALL])
    var userAnswers: MutableList<UserAnswer> = mutableListOf(),

    @OneToMany(mappedBy = "longProblem", cascade = [CascadeType.ALL], orphanRemoval = true)
    var standardAnswers: MutableList<StandardAnswer> = mutableListOf(),
) : Problem(
    title = title,
    description = description,
    creator = creator,
    dtype = "long",
    isGradable = false,
    score = gradingStandards.sumOf { it.score },
) {
    fun addGradingStandards(gradingStandards: List<GradingStandard>) {
        this.gradingStandards.addAll(gradingStandards)
        score = gradingStandards.sumOf { it.score }
    }

    fun updateFromDto(upsertRequestDto: LongProblemUpsertRequestDto) {
        title = upsertRequestDto.title
        description = upsertRequestDto.description
        isActive = upsertRequestDto.isActive
        updateStandardAnswers(upsertRequestDto.standardAnswers)
    }

    fun toLongProblemResponseDto(): LongProblemResponseDto {
        return LongProblemResponseDto(
            id,
            title,
            description,
            standardAnswers.map { it.content },
            problemTags.map { it.tag.name },
            isActive,
        )
    }

    fun toLongProblemDataDto(): LongProblemDataDto {
        val keywordScores = userAnswers.map {
            it.getKeywordScore()
        }

        val contentScores = userAnswers.map {
            it.getContentScore()
        }

        return LongProblemDataDto(
            id,
            title,
            creator.username,
            if (keywordScores.isEmpty()) null else keywordScores.average(),
            if (contentScores.isEmpty()) null else contentScores.average(),
            userAnswers.size,
            isActive,
        )
    }

    fun toDetailResponseDto(email: String?, likes: List<Like>): LongProblemDetailResponseDto {
        val tags = problemTags.map {
            it.tag
        }.map {
            it.name
        }

        val scoreList = gradingHistory.map {
            it.score
        }.toList().sorted()

        val isSolved = gradingHistory.any { it.user.email == email }

        return LongProblemDetailResponseDto(
            id,
            title,
            tags,
            description,
            if (scoreList.isEmpty()) null else scoreList.average(),
            if (scoreList.isEmpty()) null else scoreList.last(),
            if (scoreList.isEmpty()) null else scoreList.first(),
            score,
            scoreList.size,
            isSolved,
            isGradable,
            likes.count().toLong(),
            problemBookmark.count().toLong(),
            likes.any { it.user.email == email },
            problemBookmark.any { it.user.email == email },
        )
    }

    fun updateStandardAnswers(standardAnswers: List<String>) {
        if (this.standardAnswers.map { it.content }.toSet() != standardAnswers.toSet()) {
            this.standardAnswers.clear()
            this.standardAnswers.addAll(
                standardAnswers.map {
                    StandardAnswer(
                        content = it,
                        longProblem = this,
                    )
                },
            )
        }
    }
}
