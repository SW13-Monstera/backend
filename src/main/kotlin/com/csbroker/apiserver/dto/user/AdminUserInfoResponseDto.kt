package com.csbroker.apiserver.dto.user

import java.util.UUID

data class AdminUserInfoResponseDto(
    val id: UUID,
    val username: String
)
