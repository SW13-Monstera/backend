package io.csbroker.apiserver.dto.problem.multiplechoiceproblem

import io.csbroker.apiserver.dto.problem.AdminProblemResponseDto

data class MultipleChoiceProblemResponseDto(
    val id: Long,
    val title: String,
    val description: String,
    val tags: List<String>,
    val isMultiple: Boolean,
    val choiceData: List<MultipleChoiceProblemUpsertRequestDto.ChoiceData>,
    val score: Double,
    val isActive: Boolean,
    val isGradable: Boolean,
) : AdminProblemResponseDto
