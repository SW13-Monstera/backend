package io.csbroker.apiserver.dto.problem.longproblem

class ContentDto(
    val id: Long,
    val content: String,
    val isExist: Boolean = false
)
