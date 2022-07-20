package com.csbroker.apiserver.dto

import java.util.UUID

data class UserLoginResponseDto(
    val id: UUID,
    val accessToken: String
)
