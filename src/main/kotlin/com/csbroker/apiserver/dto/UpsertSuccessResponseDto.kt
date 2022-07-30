package com.csbroker.apiserver.dto

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UpsertSuccessResponseDto(
    val id: Long? = null,
    val size: Int? = null
)
