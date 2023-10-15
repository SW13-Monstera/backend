package io.csbroker.apiserver.model

import io.csbroker.apiserver.common.enums.ErrorCode
import io.csbroker.apiserver.common.exception.InternalServiceException
import io.csbroker.apiserver.controller.v2.problem.response.ShortProblemAnswerType
import io.csbroker.apiserver.controller.v2.problem.response.ShortProblemDetailResponseV2Dto
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
    var answer: String,
) : Problem(title = title, description = description, creator = creator, dtype = "short", score = score) {
    fun updateFromDto(upsertRequestDto: ShortProblemUpsertRequestDto) {
        title = upsertRequestDto.title
        description = upsertRequestDto.description
        answer = upsertRequestDto.answer
        isGradable = upsertRequestDto.isGradable
        isActive = upsertRequestDto.isActive
        score = upsertRequestDto.score
    }

    fun toShortProblemResponseDto(): ShortProblemResponseDto {
        return ShortProblemResponseDto(
            id,
            title,
            description,
            problemTags.map { it.tag.name },
            answer,
            score,
            isActive,
            isGradable,
        )
    }

    fun toShortProblemDataDto(): ShortProblemSearchResponseDto.ShortProblemDataDto {
        val answerCnt = gradingHistory.size
        val correctAnswerCnt = gradingHistory.count {
            it.score == score
        }

        return ShortProblemSearchResponseDto.ShortProblemDataDto(
            id,
            title,
            creator.username,
            if (answerCnt == 0) null else correctAnswerCnt / answerCnt.toDouble(),
            answerCnt,
            isActive,
        )
    }

    fun toDetailResponseDto(email: String?): ShortProblemDetailResponseDto {
        val commonDetail = ProblemCommonDetailResponse.getCommonDetail(this)

        return ShortProblemDetailResponseDto(
            id,
            title,
            commonDetail.tags,
            description,
            commonDetail.correctSubmission,
            commonDetail.correctUserCnt,
            commonDetail.totalSubmission,
            answer.length,
            isEnglish(),
            gradingHistory.any { it.user.email == email },
            score,
            problemLike.count().toLong(),
            problemBookmark.count().toLong(),
            problemLike.any { it.user.email == email },
            problemBookmark.any { it.user.email == email },
        )
    }

    fun toDetailResponseV2Dto(email: String?): ShortProblemDetailResponseV2Dto {
        val commonDetail = ProblemCommonDetailResponse.getCommonDetail(this)

        return ShortProblemDetailResponseV2Dto(
            id,
            title,
            commonDetail.tags,
            description,
            commonDetail.correctSubmission,
            commonDetail.correctUserCnt,
            commonDetail.totalSubmission,
            answer.length,
            getTypeOfAnswer(),
            gradingHistory.any { it.user.email == email },
            score,
        )
    }

    private fun getTypeOfAnswer(): ShortProblemAnswerType {
        return when {
            isEnglish() -> ShortProblemAnswerType.ENGLISH
            isKorean() -> ShortProblemAnswerType.KOREAN
            isNumeric() -> ShortProblemAnswerType.NUMERIC
            else -> throw InternalServiceException(ErrorCode.SERVER_ERROR, "문제 답변이 영어, 한글, 숫자가 아닙니다.")
        }
    }

    private fun isKorean(): Boolean {
        for (c in answer.replace("\\s".toRegex(), "")) {
            if (c !in 'ㄱ'..'ㅣ' && c !in '가'..'힣') {
                return false
            }
        }
        return true
    }

    private fun isEnglish(): Boolean {
        for (c in answer.replace("\\s".toRegex(), "")) {
            if (c !in 'A'..'Z' && c !in 'a'..'z') {
                return false
            }
        }
        return true
    }

    private fun isNumeric(): Boolean {
        for (c in answer.replace("\\s".toRegex(), "")) {
            if (c !in '0'..'9') {
                return false
            }
        }
        return true
    }
}
