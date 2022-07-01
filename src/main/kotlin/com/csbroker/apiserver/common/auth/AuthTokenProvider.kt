package com.csbroker.apiserver.common.auth

import com.csbroker.apiserver.common.enums.Role
import io.jsonwebtoken.security.Keys
import java.security.Key
import java.util.Date
import java.util.UUID

class AuthTokenProvider(
    secret: String
) {
    private val key: Key = Keys.hmacShaKeyFor(secret.toByteArray())

    fun createAuthToken(id: UUID, expiry: Date, role: Role? = null): AuthToken {
        return AuthToken(id, expiry, this.key, role)
    }

    fun convertAuthToken(token: String): AuthToken {
        return AuthToken(token, key)
    }
}
