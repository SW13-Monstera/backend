package com.csbroker.apiserver.dto.problem

data class MultipleProblemResponseDto(
    val id: Long,
    val title: String,
    val description: String,
    val tags: List<String>,
    val isMultiple: Boolean,
    val choiceData: List<MultipleChoiceProblemUpsertRequestDto.ChoiceData>,
    val score: Double
)
