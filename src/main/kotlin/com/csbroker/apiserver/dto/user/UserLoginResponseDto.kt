package com.csbroker.apiserver.dto.user

import com.csbroker.apiserver.common.enums.Role
import java.util.UUID

data class UserLoginResponseDto(
    val id: UUID,
    val username: String,
    val email: String,
    val role: Role,
    val accessToken: String
) {
    constructor(userLoginDto: UserLoginDto) : this(
        userLoginDto.id,
        userLoginDto.username,
        userLoginDto.email,
        userLoginDto.role,
        userLoginDto.accessToken
    )
}
