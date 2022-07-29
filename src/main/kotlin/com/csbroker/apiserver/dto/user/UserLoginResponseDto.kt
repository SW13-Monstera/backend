package com.csbroker.apiserver.dto.user

import java.util.UUID

data class UserLoginResponseDto(
    val id: UUID,
    val accessToken: String
)
