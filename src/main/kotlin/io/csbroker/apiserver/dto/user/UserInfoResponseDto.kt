package io.csbroker.apiserver.dto.user

import io.csbroker.apiserver.common.enums.Role
import com.fasterxml.jackson.annotation.JsonInclude
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserInfoResponseDto(
    val id: UUID,
    val username: String,
    val email: String,
    val role: Role,
    val accessToken: String?
) {
    constructor(userInfoDto: UserInfoDto) : this(
        userInfoDto.id,
        userInfoDto.username,
        userInfoDto.email,
        userInfoDto.role,
        userInfoDto.accessToken
    )
}
