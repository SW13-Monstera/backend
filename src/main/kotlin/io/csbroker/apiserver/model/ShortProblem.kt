package io.csbroker.apiserver.model

import io.csbroker.apiserver.controller.v2.response.ShortProblemAnswerType
import io.csbroker.apiserver.controller.v2.response.ShortProblemDetailResponseV2Dto
import io.csbroker.apiserver.dto.problem.ProblemCommonDetailResponse
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemDetailResponseDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemResponseDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemSearchResponseDto
import io.csbroker.apiserver.dto.problem.shortproblem.ShortProblemUpsertRequestDto
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
    score: Double,

    @Column(name = "answer")
    var answer: String
) : Problem(title = title, description = description, creator = creator, dtype = "short", score = score) {
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

    fun toDetailResponseDto(email: String?): ShortProblemDetailResponseDto {
        val commonDetail = ProblemCommonDetailResponse.getCommonDetail(this)

        return ShortProblemDetailResponseDto(
            this.id!!,
            this.title,
            commonDetail.tags,
            this.description,
            commonDetail.correctSubmission,
            commonDetail.correctUserCnt,
            commonDetail.totalSubmission,
            this.answer.length,
            this.isEnglish(),
            this.gradingHistory.any { it.user.email == email }
        )
    }

    fun toDetailResponseV2Dto(email: String?): ShortProblemDetailResponseV2Dto {
        val commonDetail = ProblemCommonDetailResponse.getCommonDetail(this)

        return ShortProblemDetailResponseV2Dto(
            this.id!!,
            this.title,
            commonDetail.tags,
            this.description,
            commonDetail.correctSubmission,
            commonDetail.correctUserCnt,
            commonDetail.totalSubmission,
            this.answer.length,
            this.getTypeOfAnswer(),
            this.gradingHistory.any { it.user.email == email }
        )
    }

    private fun getTypeOfAnswer(): ShortProblemAnswerType {
        return if (this.isEnglish()) {
            ShortProblemAnswerType.ENGLISH
        } else if (this.isNumeric()) {
            ShortProblemAnswerType.NUMERIC
        } else {
            ShortProblemAnswerType.KOREAN
        }
    }

    private fun isEnglish(): Boolean {
        for (c in this.answer.replace("\\s".toRegex(), "")) {
            if (c !in 'A'..'Z' && c !in 'a'..'z' && c !in '0'..'9') {
                return false
            }
        }
        return true
    }

    private fun isNumeric(): Boolean {
        for (c in this.answer.replace("\\s".toRegex(), "")) {
            if (c !in '0'..'9') {
                return false
            }
        }
        return true
    }
}
