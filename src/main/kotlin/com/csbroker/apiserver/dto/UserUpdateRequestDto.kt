package com.csbroker.apiserver.dto

data class UserUpdateRequestDto(
    val profileImageUrl: String?,
    val username: String?,
    var password: String?
)
