package com.csbroker.apiserver.dto.problem

data class ProblemSearchDto(
    val tags: List<String> = listOf(),
    val solvedBy: String? = null,
    val query: String = "",
    val type: String = ""
)
