package com.csbroker.apiserver.dto.user

data class GithubEmailResponseDto(
    val email: String,
    val verified: Boolean,
    val primary: Boolean,
    val visibility: String?
)
