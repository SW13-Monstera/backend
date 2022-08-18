package com.csbroker.apiserver.dto.problem

import org.springframework.data.domain.Page

data class ProblemPageResponseDto(
    val contents: List<ProblemResponseDto>,
    val totalPages: Int,
    val totalElements: Long,
    val numberOfElements: Int,
    val size: Int
) {
    constructor(pageData: Page<ProblemResponseDto>) : this(
        pageData.content,
        pageData.totalPages,
        pageData.totalElements,
        pageData.numberOfElements,
        pageData.size
    )
}
