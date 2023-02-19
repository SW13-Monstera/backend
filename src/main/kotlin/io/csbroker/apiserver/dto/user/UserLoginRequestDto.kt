package io.csbroker.apiserver.dto.user

data class UserLoginRequestDto(
    val email: String,
    val password: String,
)
