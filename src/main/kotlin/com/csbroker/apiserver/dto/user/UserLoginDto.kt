package com.csbroker.apiserver.dto.user

import com.csbroker.apiserver.common.enums.Role
import com.csbroker.apiserver.model.User
import java.util.UUID

data class UserLoginDto(
    val id: UUID,
    val username: String,
    val email: String,
    val role: Role,
    val accessToken: String,
    val refreshToken: String?
) {
    constructor(user: User, accessToken: String, refreshToken: String?) : this(
        user.id!!,
        user.username,
        user.email,
        user.role,
        accessToken,
        refreshToken
    )
}
