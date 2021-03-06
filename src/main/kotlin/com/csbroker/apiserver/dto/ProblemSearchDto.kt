package com.csbroker.apiserver.dto

data class ProblemSearchDto(
    val tags: List<String> = listOf(),
    val solvedBy: String? = null,
    val query: String = ""
)
