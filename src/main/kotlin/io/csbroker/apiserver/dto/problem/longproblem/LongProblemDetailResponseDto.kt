package io.csbroker.apiserver.dto.problem.longproblem

data class LongProblemDetailResponseDto(
    val id: Long,
    val title: String,
    val tags: List<String>,
    val description: String,
    val avgScore: Double?,
    val topScore: Double?,
    val bottomScore: Double?,
    val score: Double,
    val totalSubmission: Int,
    val isSolved: Boolean,
    val isGradable: Boolean,
    val likeCount: Long,
    val bookmarkCount: Long,
    val isLiked: Boolean,
    val isBookmarked: Boolean,
)
