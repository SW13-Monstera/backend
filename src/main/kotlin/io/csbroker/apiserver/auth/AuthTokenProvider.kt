package io.csbroker.apiserver.auth

import io.csbroker.apiserver.common.enums.ErrorCode
import io.csbroker.apiserver.common.exception.UnAuthorizedException
import io.jsonwebtoken.security.Keys
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import java.security.Key
import java.util.Date

class AuthTokenProvider(
    secret: String,
) {
    private val key: Key = Keys.hmacShaKeyFor(secret.toByteArray())

    fun createAuthToken(email: String, expiry: Date, role: String? = null): AuthToken {
        return AuthToken(email, expiry, key, role)
    }

    fun convertAuthToken(token: String): AuthToken {
        return AuthToken(token, key)
    }

    fun getAuthentication(authToken: AuthToken): Authentication {
        if (authToken.isValid) {
            val claims = authToken.tokenClaims
            val authorities = arrayOf(claims!![AUTHORITIES_KEY].toString())
                .map(::SimpleGrantedAuthority)
                .toList()
            return UsernamePasswordAuthenticationToken(User(claims.subject, "", authorities), authToken, authorities)
        }

        throw UnAuthorizedException(ErrorCode.TOKEN_INVALID, "올바르지 않은 Token입니다.")
    }
}
