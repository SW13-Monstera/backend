package io.csbroker.apiserver.dto.common

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UpsertSuccessResponseDto(
    val id: Any? = null,
    val size: Int? = null,
)
