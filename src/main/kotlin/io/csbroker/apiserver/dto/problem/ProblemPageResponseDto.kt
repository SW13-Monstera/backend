package io.csbroker.apiserver.dto.problem

import org.springframework.data.domain.Page

data class ProblemPageResponseDto(
    val contents: List<ProblemResponseDto>,
    val currentPage: Int,
    val totalPages: Int,
    val totalElements: Long,
    val numberOfElements: Int,
    val size: Int
) {
    constructor(pageData: Page<ProblemResponseDto>) : this(
        pageData.content,
        pageData.pageable.pageNumber,
        pageData.totalPages,
        pageData.totalElements,
        pageData.numberOfElements,
        pageData.size
    )
}
