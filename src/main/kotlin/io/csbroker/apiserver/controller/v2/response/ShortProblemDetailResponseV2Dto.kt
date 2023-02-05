package io.csbroker.apiserver.controller.v2.response

class ShortProblemDetailResponseV2Dto(
    val id: Long,
    val title: String,
    val tags: List<String>,
    val description: String,
    val correctSubmission: Int,
    val correctUserCnt: Int,
    val totalSubmission: Int,
    val answerLength: Int,
    val consistOf: ShortProblemAnswerType,
    val isSolved: Boolean,
    val score: Double,
)

enum class ShortProblemAnswerType {
    ENGLISH,
    KOREAN,
    NUMERIC
}
