package io.csbroker.apiserver.dto.user

import io.csbroker.apiserver.common.enums.Role
import io.csbroker.apiserver.model.User
import java.util.UUID

data class UserInfoDto(
    val id: UUID,
    val username: String,
    val email: String,
    val role: Role,
    val accessToken: String?,
    val refreshToken: String?,
) {
    constructor(user: User, accessToken: String? = null, refreshToken: String? = null) : this(
        user.id!!,
        user.username,
        user.email,
        user.role,
        accessToken,
        refreshToken,
    )
}
