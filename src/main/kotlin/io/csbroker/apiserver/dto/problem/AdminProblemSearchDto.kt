package io.csbroker.apiserver.dto.problem

import org.springframework.data.domain.Pageable

data class AdminProblemSearchDto(
    val id: Long?,
    val title: String?,
    val description: String?,
    val pageable: Pageable,
)
