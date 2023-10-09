package io.csbroker.apiserver.dto.problem.shortproblem

data class ShortProblemDetailResponseDto(
    val id: Long,
    val title: String,
    val tags: List<String>,
    val description: String,
    val correctSubmission: Int,
    val correctUserCnt: Int,
    val totalSubmission: Int,
    val answerLength: Int,
    val isEnglish: Boolean,
    val isSolved: Boolean,
    val score: Double,
    val likeCount: Long,
    val bookmarkCount: Long,
    val isLiked: Boolean,
    val isBookmarked: Boolean,
)
