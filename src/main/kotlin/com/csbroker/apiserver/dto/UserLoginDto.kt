package com.csbroker.apiserver.dto

import java.util.UUID

data class UserLoginDto(
    val id: UUID,
    val accessToken: String,
    val refreshToken: String?
)
