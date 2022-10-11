package io.csbroker.apiserver.dto.problem.grade

data class GradeResultDto(
    val correctKeywordIds: List<Long>,
    val predictKeywordPositions: Map<Long, List<Int>> = mapOf(),
    val correctContentIds: List<Long> = arrayListOf()
) {
    constructor(keywordGradingResponseDto: KeywordGradingResponseDto) : this(
        keywordGradingResponseDto.correct_keywords.map { it.id },
        keywordGradingResponseDto.correct_keywords.associate { it.id to it.predict_keyword_position }
    )

    constructor(gradingResponseDto: GradingResponseDto) : this(
        gradingResponseDto.correct_keywords.map { it.id },
        gradingResponseDto.correct_keywords.associate { it.id to it.predict_keyword_position },
        gradingResponseDto.correct_contents.map { it.id }
    )
}
