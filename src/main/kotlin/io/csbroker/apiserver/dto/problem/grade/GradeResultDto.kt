package io.csbroker.apiserver.dto.problem.grade

data class GradeResultDto(
    val correctKeywordIds: List<Long>,
    val predictKeywordPositions: Map<Long, List<Int>> = mapOf(),
    val correctContentIds: List<Long> = arrayListOf(),
) {
    constructor(keywordGradingResponseDto: KeywordGradingResponseDto) : this(
        keywordGradingResponseDto.correctKeywords.map { it.id },
        keywordGradingResponseDto.correctKeywords.associate { it.id to it.predictKeywordPosition },
    )

    constructor(gradingResponseDto: GradingResponseDto) : this(
        gradingResponseDto.correctKeywords.map { it.id },
        gradingResponseDto.correctKeywords.associate { it.id to it.predictKeywordPosition },
        gradingResponseDto.correctContents.map { it.id },
    )
}
