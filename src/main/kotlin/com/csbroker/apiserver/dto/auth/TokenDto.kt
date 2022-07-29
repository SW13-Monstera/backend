package com.csbroker.apiserver.dto.auth

data class TokenDto(
    val accessToken: String,
    val refreshToken: String?
)
