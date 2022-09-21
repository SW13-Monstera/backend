package com.csbroker.apiserver.dto.user

import com.csbroker.apiserver.common.enums.Role
import java.util.UUID

data class UserResponseDto(
    val id: UUID,
    val email: String,
    val username: String,
    val role: Role,
    val major: String?,
    val job: String?,
    val jobObjective: String?,
    val techs: List<String>,
    val profileImgUrl: String?,
    var githubUrl: String?,
    var linkedinUrl: String?
)
