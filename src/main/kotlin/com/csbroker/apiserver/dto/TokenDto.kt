package com.csbroker.apiserver.dto

data class TokenDto(
    val accessToken: String,
    val refreshToken: String?
)
