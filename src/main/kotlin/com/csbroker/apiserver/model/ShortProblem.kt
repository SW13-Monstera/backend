package com.csbroker.apiserver.model

import com.csbroker.apiserver.dto.problem.shortproblem.ShortProblemDetailResponseDto
import com.csbroker.apiserver.dto.problem.shortproblem.ShortProblemResponseDto
import com.csbroker.apiserver.dto.problem.shortproblem.ShortProblemSearchResponseDto
import com.csbroker.apiserver.dto.problem.shortproblem.ShortProblemUpsertRequestDto
import javax.persistence.Column
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "short_problem")
@DiscriminatorValue("short")
class ShortProblem(
    title: String,
    description: String,
    creator: User,

    @Column(name = "answer")
    var answer: String,

    @Column(name = "score")
    var score: Double
) : Problem(title = title, description = description, creator = creator, dtype = "short") {
    fun updateFromDto(upsertRequestDto: ShortProblemUpsertRequestDto) {
        this.title = upsertRequestDto.title
        this.description = upsertRequestDto.description
        this.answer = upsertRequestDto.answer
        this.isGradable = upsertRequestDto.isGradable
        this.isActive = upsertRequestDto.isActive
        this.score = upsertRequestDto.score
    }

    fun toShortProblemResponseDto(): ShortProblemResponseDto {
        return ShortProblemResponseDto(
            this.id!!,
            this.title,
            this.description,
            this.problemTags.map { it.tag.name },
            this.answer,
            this.score,
            this.isActive,
            this.isGradable
        )
    }

    fun toShortProblemDataDto(): ShortProblemSearchResponseDto.ShortProblemDataDto {
        val answerCnt = this.gradingHistory.size
        val correctAnswerCnt = this.gradingHistory.count {
            it.score == this.score
        }

        return ShortProblemSearchResponseDto.ShortProblemDataDto(
            this.id!!,
            this.title,
            this.creator.username,
            if (answerCnt == 0) null else correctAnswerCnt / answerCnt.toDouble(),
            answerCnt,
            this.isActive
        )
    }

    fun toDetailResponseDto(): ShortProblemDetailResponseDto {
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

        return ShortProblemDetailResponseDto(
            this.id!!,
            this.title,
            tags,
            this.description,
            scoreList.count { it == this.score },
            scoreList.count { it != this.score },
            totalSolved,
            this.answer.length,
            this.isEnglish()
        )
    }

    private fun isEnglish(): Boolean {
        for (c in this.answer) {
            if (c !in 'A'..'Z' && c !in 'a'..'z' && c !in '0'..'9') {
                return false
            }
        }
        return true
    }
}
