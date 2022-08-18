package com.csbroker.apiserver.dto.problem.longproblem

data class KeywordDto(
    val id: Long,
    val content: String,
    val isExist: Boolean = false,
    val idx: List<Int> = listOf()
)
