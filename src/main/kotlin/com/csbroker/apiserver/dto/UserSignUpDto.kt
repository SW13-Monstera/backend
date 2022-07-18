package com.csbroker.apiserver.dto

import com.csbroker.apiserver.common.auth.ProviderType
import com.csbroker.apiserver.model.User

data class UserSignUpDto(
    val email: String,
    val username: String,
    val password: String
) {
    fun toUser(): User {
        return User(
            email = email,
            username = username,
            password = password,
            providerType = ProviderType.LOCAL
        )
    }
}
