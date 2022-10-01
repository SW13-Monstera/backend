package io.csbroker.apiserver.dto.problem

data class ProblemSearchDto(
    val tags: List<String>? = listOf(),
    val solvedBy: String?,
    val isSolved: Boolean?,
    val query: String?,
    val type: List<String>? = listOf(),
    val isGradable: Boolean?
)
