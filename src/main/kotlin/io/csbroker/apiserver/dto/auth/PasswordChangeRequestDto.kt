package io.csbroker.apiserver.dto.auth

data class PasswordChangeRequestDto(
    val code: String,
    val password: String,
)
