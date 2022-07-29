package com.csbroker.apiserver.dto.user

data class UserLoginRequestDto(
    val email: String,
    val password: String
)
