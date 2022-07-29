package com.csbroker.apiserver.dto.user

import com.csbroker.apiserver.common.enums.Role
import java.util.UUID

data class UserResponseDto(
    val id: UUID,
    val email: String,
    val username: String,
    val role: Role
)
