package com.csbroker.apiserver.dto.user

data class UserUpdateRequestDto(
    val profileImageUrl: String?,
    val username: String?,
    var password: String?
)