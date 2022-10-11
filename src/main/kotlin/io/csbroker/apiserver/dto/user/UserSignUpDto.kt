package io.csbroker.apiserver.dto.user

import io.csbroker.apiserver.auth.ProviderType
import io.csbroker.apiserver.model.User

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
