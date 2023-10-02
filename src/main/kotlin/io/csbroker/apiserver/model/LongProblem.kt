package io.csbroker.apiserver.model

import io.csbroker.apiserver.dto.problem.longproblem.LongProblemDetailResponseDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemResponseDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemSearchResponseDto.LongProblemDataDto
import io.csbroker.apiserver.dto.problem.longproblem.LongProblemUpsertRequestDto
import io.csbroker.apiserver.dto.user.GradingStandardResponseDto
import javax.persistence.CascadeType
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

    @OneToMany(mappedBy = "problem", cascade = [CascadeType.ALL])
    var gradingStandards: MutableList<GradingStandard> = mutableListOf(),

    @OneToMany(mappedBy = "problem", cascade = [CascadeType.ALL])
    var userAnswers: MutableList<UserAnswer> = mutableListOf(),

    @OneToMany(mappedBy = "longProblem", cascade = [CascadeType.ALL])
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
        isGradable = upsertRequestDto.isGradable
        isActive = upsertRequestDto.isActive
    }

    fun toLongProblemResponseDto(): LongProblemResponseDto {
        return LongProblemResponseDto(
            id!!,
            title,
            description,
            standardAnswers.map { it.content },
            problemTags.map { it.tag.name },
            gradingStandards.map { GradingStandardResponseDto.fromGradingStandard(it) },
            isActive,
            isGradable,
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
            id!!,
            title,
            creator.username,
            if (keywordScores.isEmpty()) null else keywordScores.average(),
            if (contentScores.isEmpty()) null else contentScores.average(),
            userAnswers.size,
            isActive,
        )
    }

    fun toDetailResponseDto(email: String?): LongProblemDetailResponseDto {
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
            id!!,
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
            problemLike.count().toLong(),
            problemBookmark.count().toLong(),
            problemLike.any { it.user.email == email },
            problemBookmark.any { it.user.email == email },
        )
    }
}
