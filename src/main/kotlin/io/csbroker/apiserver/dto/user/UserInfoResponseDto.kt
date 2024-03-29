package io.csbroker.apiserver.dto.user

import com.fasterxml.jackson.annotation.JsonInclude
import io.csbroker.apiserver.common.enums.Role
import io.csbroker.apiserver.model.User
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserInfoResponseDto(
    val id: UUID,
    val username: String,
    val email: String,
    val role: Role,
    val accessToken: String?,
) {
    constructor(userInfoDto: UserInfoDto) : this(
        userInfoDto.id,
        userInfoDto.username,
        userInfoDto.email,
        userInfoDto.role,
        userInfoDto.accessToken,
    )

    constructor(user: User, accessToken: String? = null) : this(
        user.id!!,
        user.username,
        user.email,
        user.role,
        accessToken,
    )
}
